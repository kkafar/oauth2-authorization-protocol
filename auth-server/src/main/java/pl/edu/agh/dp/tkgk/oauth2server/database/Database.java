package pl.edu.agh.dp.tkgk.oauth2server.database;

import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.util.Optional;

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
}
