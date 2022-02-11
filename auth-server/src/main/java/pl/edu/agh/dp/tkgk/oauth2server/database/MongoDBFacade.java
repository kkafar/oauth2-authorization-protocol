package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jetbrains.annotations.NotNull;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Session;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public final class MongoDBFacade implements Database {

    private MongoDatabase database = MongoClientInstance.getDatabase();

    private final Queries queries = new Queries();
    private final Random random;
    private final Map<String, Session> sessionMap;

    private final static long CODE_LIFE_TIME_IN_SECONDS = 120;
    public final static long SESSION_LIFE_TIME_IN_SECONDS = 1200;


    private MongoDBFacade() {
        sessionMap = new ConcurrentHashMap<>();
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

        Token tokenObj = new Token(tokenId, token, authorizationCode, clientId);

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
        if(!sessionMap.containsKey(sessionId)) return false;
        Session session = sessionMap.get(sessionId);
        return session.getExpireTimeInSeconds() > Instant.now().getEpochSecond();
    }

    @Override
    public boolean areCredentialsValid(Credentials credentials) {

        return false;
    }

    @Override
    public String createNewSession(String login) {
        String session_id = getRandomString(128);
        long expireTime = Instant.now().getEpochSecond() + SESSION_LIFE_TIME_IN_SECONDS;
        Session session = new Session(session_id, login, expireTime);
        sessionMap.put(session_id, session);
        return session_id;
    }

    @Override
    public String generateCode(AuthorizationRequest request) {
        String code = getRandomString(64);
        long expireTime = Instant.now().getEpochSecond() + CODE_LIFE_TIME_IN_SECONDS;
        AuthCode authCode =
                new AuthCode(code, request.codeChallenge, request.codeChallengeMethod, expireTime,
                        "client", false, List.of("all"));

        MongoCollection<AuthCode> authCodeMongoCollection =
                getCollection(AuthCode.class, MongoDBInfo.Collections.AUTH_CODES_COLLECTION.toString());
        queries.addObjectToCollection(authCode, authCodeMongoCollection);

        return code;
    }

    @NotNull
    private String getRandomString(int x) {
        byte[] randomBytes = new byte[x];
        random.nextBytes(randomBytes);
        return new String(Base64.getUrlEncoder().encode(randomBytes));
    }

    // for testing purposes now only
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }
}
