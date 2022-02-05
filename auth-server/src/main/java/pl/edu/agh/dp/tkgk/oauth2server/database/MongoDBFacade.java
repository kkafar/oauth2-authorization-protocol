package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;

import java.util.Optional;

public class MongoDBFacade implements Database {

    private MongoDatabase database = MongoClientInstance.getDatabase();

    private final Queries queries = new Queries();

    private MongoDBFacade() { }

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
            tokenAndAssociatedTokensRevocation(refreshTokens, accessTokens, tokenId, authCode);
        } else if (tokenHint == TokenHint.REFRESH_TOKEN) {
            tokenAndAssociatedTokensRevocation(accessTokens, refreshTokens, tokenId, authCode);
        }
    }

    private void tokenAndAssociatedTokensRevocation(MongoCollection<Token> expectedTokens,
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
        return Optional.ofNullable(queries.getObjectFromCollection(authCodes, Client.JsonFields.ID, authorizationCode));
    }

    @Override
    public boolean isSessionIdValid(String sessionId) {
        return false;
    }

    @Override
    public boolean areCredentialsValid(Credentials credentials) {
        return false;
    }

    @Override
    public String createNewSession(String login) {
        return null;
    }

    @Override
    public String generateCode(AuthorizationRequest request) {
        return null;
    }

    // for testing purposes now only
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }
}
