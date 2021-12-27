package pl.edu.agh.dp.tkgk.oauth2server.database;

public interface Database {

    /**
     * @return true if access token was revoked
     */
    boolean revokeAccessToken();

    /**
     * @return true if refresh token was revoked
     */
    boolean revokeRefreshToken();
}
