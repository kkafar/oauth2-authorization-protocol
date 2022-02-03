package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;

import java.util.Optional;

public interface Database {

    Optional<Client> getClient(String clientId);

    /**
     * @param isAccessToken - true iff token hint was added to request and was equal "access_token", false otherwise
     * If token hint tells that token is an access token, then try to remove it from the access token collection,
     * otherwise try to remove it from the refresh token collection (or if the token hint was invalid/not added to request)
     */
    void tokenRevocation(DecodedJWT decodedToken, boolean isAccessToken);

    /**
     * @return token data for token introspection if token is stored in the db, otherwise return empty Optional
     */
    Optional<JSONObject> fetchTokenData();

    boolean isSessionIdValid(String sessionId);

    boolean areCredentialsValid(Credentials credentials);

    String createNewSession(String login);

    String generateCode(AuthorizationRequest request);

    Optional<String> getAuthorizationRedirectUri(String authorizationCodeString);
}
