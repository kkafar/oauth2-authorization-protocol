package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.DecodedToken;
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
    public Optional<JSONObject> fetchTokenData() {
        return Optional.empty();
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
    public void tokenRevocation(DecodedJWT decodedToken, boolean isAccessToken) {

        MongoCollection<Token> accessTokens =
                getCollection(Token.class, MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString());

        MongoCollection<Token> refreshTokens =
                getCollection(Token.class, MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString());

        String authCodeValue = decodedToken.getClaim(DecodedToken.Claims.AUTH_CODE).asString();

        if (isAccessToken) {
            queries.deleteObjectFromCollection(accessTokens, Token.JsonFields.ID, decodedToken.getId());
            queries.deleteObjectsFromCollection(refreshTokens, Token.JsonFields.AUTH_CODE, authCodeValue);
        } else {
            queries.deleteObjectFromCollection(refreshTokens, Token.JsonFields.ID, decodedToken.getId());
            queries.deleteObjectsFromCollection(accessTokens, Token.JsonFields.AUTH_CODE, authCodeValue);
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
