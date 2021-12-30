package pl.edu.agh.dp.tkgk.oauth2server.database;

import org.json.JSONObject;
import java.util.Optional;

import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

public interface Database {

    Optional<Client> getClient(String clientId);

    /**
     * @return true if access token was revoked
     */
    boolean revokeAccessToken();

    /**
     * @return true if refresh token was revoked
     */
    boolean revokeRefreshToken();

    /**
     * @return token data for token introspection if token is stored in the db, otherwise return empty Optional
     */
    Optional<JSONObject> fetchTokenData();

    boolean isSessionIdValid(String sessionId);

    boolean areCredentialsValid(Credentials credentials);

    String createNewSession(String login);

    String generateCode(AuthorizationRequest request);
}
