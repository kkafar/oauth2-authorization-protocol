package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Session;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;

import java.time.Instant;
import java.util.*;

public class MongoDBFacade implements Database {

    private MongoDatabase database = MongoClientInstance.getDatabase();

    private final Queries queries = new Queries();

    // mock area
    private final HashMap<String, Session> sessionHashMap;
    private final HashMap<String, AuthCode> authCodeHashMap;
    private final Random random;
    private final static int SESSION_LIFE_TIME_IN_SECONDS = 1200;
    private final static long CODE_LIFE_TIME_IN_SECONDS = 120;
    private final Credentials validCredentials = new Credentials("ala", "1234");
    // mock area end

    private MongoDBFacade() {
        sessionHashMap = new HashMap<>();
        authCodeHashMap = new HashMap<>();
        random = new Random();
    }

    private static class AuthorizationDatabaseFacadeHolder {
        private static final MongoDBFacade facade = new MongoDBFacade();
    }

    public static MongoDBFacade getInstance(){
        return AuthorizationDatabaseFacadeHolder.facade;
    }

    private <T> MongoCollection<T> getCollection(Class<T> type, String collectionName) {
        return database.getCollection(collectionName, type);
    }

    @Override
    public Optional<Token> fetchToken(String tokenId, TokenHint tokenHint) {
        MongoCollection<Token> accessTokens =
                getCollection(Token.class, MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString());

        MongoCollection<Token> refreshTokens =
                getCollection(Token.class, MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString());

        if (tokenHint == TokenHint.ACCESS_TOKEN) {
            return fetchTokenWithTwoTries(tokenId, accessTokens, refreshTokens);
        }

        return fetchTokenWithTwoTries(tokenId, refreshTokens, accessTokens);
    }

    private Optional<Token> fetchTokenWithTwoTries(String tokenId, MongoCollection<Token> expectedTokens,
                                                   MongoCollection<Token> otherTokens)
    {
        Token token = queries.getObjectFromCollection(expectedTokens, Token.JsonFields.ID, tokenId);
        if (token == null) {
            return Optional.ofNullable(queries.getObjectFromCollection(otherTokens, Token.JsonFields.ID, tokenId));
        }
        return Optional.of(token);
    }

    @Override
    public void tokenRevocation(DecodedJWT decodedToken, TokenHint tokenHint) {

        MongoCollection<Token> accessTokens =
                getCollection(Token.class, MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString());

        MongoCollection<Token> refreshTokens =
                getCollection(Token.class, MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString());

        String authCode = decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE).asString();

        String tokenId = decodedToken.getId();

        if (tokenHint == TokenHint.NO_TOKEN_HINT) {
            if (queries.getObjectFromCollection(accessTokens, Token.JsonFields.ID, tokenId) != null) {
                tokenHint = TokenHint.ACCESS_TOKEN;
            } else if (queries.getObjectFromCollection(refreshTokens, Token.JsonFields.ID, tokenId) != null) {
                tokenHint = TokenHint.REFRESH_TOKEN;
            }
        }

        if (tokenHint == TokenHint.ACCESS_TOKEN) {
            tokenAndAssociatedTokensRemoval(refreshTokens, accessTokens, tokenId, authCode);
        } else if (tokenHint == TokenHint.REFRESH_TOKEN) {
            tokenAndAssociatedTokensRemoval(accessTokens, refreshTokens, tokenId, authCode);
        }
    }

    private void tokenAndAssociatedTokensRemoval(MongoCollection<Token> expectedTokens,
                                                 MongoCollection<Token> otherTokens,
                                                 String tokenId, String authCode)
    {
        if (queries.deleteObjectFromCollection(otherTokens, Token.JsonFields.ID, tokenId)) {
            queries.deleteObjectsFromCollection(expectedTokens, Token.JsonFields.AUTH_CODE, authCode);
        } else {
            if (!queries.deleteObjectFromCollection(expectedTokens, Token.JsonFields.ID, tokenId)) return;
            queries.deleteObjectsFromCollection(otherTokens, Token.JsonFields.AUTH_CODE, authCode);
        }
    }

    @Override
    public Optional<Client> fetchClient(String clientId) {
        MongoCollection<Client> clients = getCollection(Client.class, MongoDBInfo.Collections.CLIENTS_COLLECTION.toString());
        return Optional.ofNullable(queries.getObjectFromCollection(clients, Client.JsonFields.ID, clientId));
    }

    @Override
    public Optional<AuthCode> fetchAuthorizationCode(String authorizationCode) {
        MongoCollection<AuthCode> authCodes = getCollection(AuthCode.class, MongoDBInfo.Collections.AUTH_CODES_COLLECTION.toString());
        return Optional.ofNullable(queries.getObjectFromCollection(authCodes, AuthCode.JsonFields.ID, authorizationCode));
    }

    @Override
    public Token getNewToken(int expiresIn, List<String> scope, String authorizationCode,
                             boolean isAccessToken, String tokenType, String clientId)
    {
        String tokenId = getUniqueTokenId();
        String token = TokenUtil.generateToken(expiresIn, scope, authorizationCode, isAccessToken, tokenType, tokenId);

        Token tokenObj = new Token(authorizationCode, token, tokenId, clientId);

        String tokensCollection = isAccessToken ? MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString()
                : MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString();

        MongoCollection<Token> tokens = getCollection(Token.class, tokensCollection);
        queries.addObjectToCollection(tokenObj, tokens);

        return tokenObj;
    }

    @Override
    public Token getNewTokenFromAuthCode(int expiresIn, AuthCode authorizationCode,
                             boolean isAccessToken, String tokenType)
    {
        Token token = getNewToken(expiresIn, authorizationCode.getScope(), authorizationCode.getCode(),
                isAccessToken, tokenType, authorizationCode.getClientId());

        MongoCollection<AuthCode> authCodes =
                getCollection(AuthCode.class, MongoDBInfo.Collections.AUTH_CODES_COLLECTION.toString());

        queries.updateObjectFromCollection(authCodes, AuthCode.JsonFields.ID, authorizationCode.getCode(),
                AuthCode.JsonFields.USED, true);

        return token;
    }

    private String getUniqueTokenId() {
        Optional<Token> token;
        String tokenId;

        do {
            tokenId = TokenUtil.generateTokenId();
            token = fetchToken(tokenId, TokenHint.NO_TOKEN_HINT);
        } while (token.isPresent());

        return tokenId;
    }

    @Override
    public boolean isSessionIdValid(String sessionId) {
        if(!sessionHashMap.containsKey(sessionId)) return false;
        Session session = sessionHashMap.get(sessionId);
        return session.getExpireTimeInSeconds() > Instant.now().getEpochSecond();
    }

    @Override
    public boolean areCredentialsValid(Credentials credentials) {
        return validCredentials.equals(credentials);
    }

    @Override
    public String createNewSession(String login) {
        byte[] randomBytes = new byte[128];
        random.nextBytes(randomBytes);
        String session_id = new String(Base64.getUrlEncoder().encode(randomBytes));
        long expireTime = Instant.now().getEpochSecond() + SESSION_LIFE_TIME_IN_SECONDS;
        Session session = new Session(session_id, login, expireTime);
        sessionHashMap.put(session_id, session);
        return session_id;
    }

    @Override
    public String generateCode(AuthorizationRequest request) {
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        String code = new String(Base64.getUrlEncoder().encode(randomBytes));
        long expireTime = Instant.now().getEpochSecond() + CODE_LIFE_TIME_IN_SECONDS;
        AuthCode authCode =
                new AuthCode(code, request.codeChallenge, request.codeChallengeMethod, expireTime,
                        "client", false, List.of("all")); // AuthCode needs clientId and used parameters, so I added them here
        authCodeHashMap.put(code, authCode);
        return code;
    }

    // for testing purposes now only
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }
}
