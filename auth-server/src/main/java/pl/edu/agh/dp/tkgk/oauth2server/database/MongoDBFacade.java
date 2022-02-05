package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
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
    public Optional<Client> getClient(String clientId) {
        return Optional.empty();
    }

    @Override
    public Optional<Token> fetchToken(DecodedJWT decodedToken, TokenHint tokenHint) {
        MongoCollection<Token> accessTokens =
                getCollection(Token.class, MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString());

        MongoCollection<Token> refreshTokens =
                getCollection(Token.class, MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString());

        if (tokenHint == TokenHint.ACCESS_TOKEN) {
            return Optional.ofNullable(queries.getObjectFromCollection(accessTokens, Token.JsonFields.ID, decodedToken.getId()));
        } else if (tokenHint == TokenHint.REFRESH_TOKEN) {
            return Optional.ofNullable(queries.getObjectFromCollection(refreshTokens, Token.JsonFields.ID, decodedToken.getId()));
        } else {
            Token token = queries.getObjectFromCollection(accessTokens, Token.JsonFields.ID, decodedToken.getId());
            if (token == null) {
                return Optional.ofNullable(queries.getObjectFromCollection(refreshTokens, Token.JsonFields.ID, decodedToken.getId()));
            }
            return Optional.of(token);
        }
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

    @Override
    public void tokenRevocation(DecodedJWT decodedToken, TokenHint tokenHint) {

        MongoCollection<Token> accessTokens =
                getCollection(Token.class, MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString());

        MongoCollection<Token> refreshTokens =
                getCollection(Token.class, MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString());

        String authCodeValue = decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE).asString();

        String tokenId = decodedToken.getId();

        if (tokenHint == TokenHint.NO_TOKEN_HINT) {
            if (queries.getObjectFromCollection(accessTokens, Token.JsonFields.ID, tokenId) != null) {
                tokenHint = TokenHint.ACCESS_TOKEN;
            } else if (queries.getObjectFromCollection(refreshTokens, Token.JsonFields.ID, tokenId) != null) {
                tokenHint = TokenHint.REFRESH_TOKEN;
            }
        }

        if (tokenHint == TokenHint.ACCESS_TOKEN) {
            tokenAndAssociatedTokensRevocation(refreshTokens, accessTokens, tokenId, authCodeValue);
        } else if (tokenHint == TokenHint.REFRESH_TOKEN) {
            tokenAndAssociatedTokensRevocation(accessTokens, refreshTokens, tokenId, authCodeValue);
        }
    }

    private void tokenAndAssociatedTokensRevocation(MongoCollection<Token> tokensCollection1,
                                                    MongoCollection<Token> tokensCollection2,
                                                    String tokenId, String authCodeValue)
    {
        if (queries.deleteObjectFromCollection(tokensCollection2, Token.JsonFields.ID, tokenId)) {
            queries.deleteObjectsFromCollection(tokensCollection1, Token.JsonFields.AUTH_CODE, authCodeValue);
        } else {
            if (!queries.deleteObjectFromCollection(tokensCollection1, Token.JsonFields.ID, tokenId)) return;
            queries.deleteObjectsFromCollection(tokensCollection2, Token.JsonFields.AUTH_CODE, authCodeValue);
        }
    }

    @Override
    public Optional<String> getAuthorizationRedirectUri(String authorizationCodeString) {
        return Optional.empty();
    }

    // for testing purposes now only
    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }
}
