package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.TokenQueries;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

class MongoDBFacade implements Database {

    private final MongoDatabase db = MongoClientInstance.getDatabase();

    private final TokenQueries tokenQueries = new TokenQueries();

    private MongoDBFacade(){}

    private static class AuthorizationDatabaseFacadeHolder{
        private static final MongoDBFacade database = new MongoDBFacade();
    }

    public static MongoDBFacade getInstance(){
        return AuthorizationDatabaseFacadeHolder.database;
    }

    private <T> MongoCollection<T> getCollection(Class<T> type, String collectionName) {
        return db.getCollection(collectionName, type);
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
        Bson idFilter = eq("_id", decodedToken.getId());
        Bson authCodeFilter = eq("auth_code", decodedToken.getClaim(DecodedToken.Claims.AUTH_CODE));

        MongoCollection<Token> accessTokens =
                getCollection(Token.class, MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString());

        MongoCollection<Token> refreshTokens =
                getCollection(Token.class, MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString());

        if (isAccessToken) {
            tokenQueries.deleteTokens(accessTokens, idFilter);
            tokenQueries.deleteTokens(refreshTokens, authCodeFilter);
        } else {
            tokenQueries.deleteTokens(refreshTokens, idFilter);
            tokenQueries.deleteTokens(accessTokens, authCodeFilter);
        }
    }

    @Override
    public Optional<String> getAuthorizationRedirectUri(String authorizationCodeString) {
        return Optional.empty();
    }
}
