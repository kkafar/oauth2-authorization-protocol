package pl.edu.agh.dp.tkgk.oauth2server.database;

import org.json.JSONObject;

import java.util.Optional;

public interface Database {

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
}
