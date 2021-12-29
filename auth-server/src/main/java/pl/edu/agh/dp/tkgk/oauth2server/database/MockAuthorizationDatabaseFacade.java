package pl.edu.agh.dp.tkgk.oauth2server.database;

import org.json.JSONObject;

import java.util.Optional;

class MockAuthorizationDatabaseFacade implements Database{

    private MockAuthorizationDatabaseFacade(){}

    private static class MockAuthorizationDatabaseFacadeHolder{
        public static  final MockAuthorizationDatabaseFacade database = new MockAuthorizationDatabaseFacade();
    }

    public static MockAuthorizationDatabaseFacade getInstance(){
        return MockAuthorizationDatabaseFacadeHolder.database;
    }

    @Override
    public boolean revokeAccessToken() {
        // RFC7009 says that we MAY revoke all refresh tokens assigned to the same authorization grant
        return true;
    }

    @Override
    public boolean revokeRefreshToken() {
        // after revoking refresh token revoke all access tokens with the same authorization grant
        revokeAllAccessTokensWithGivenGrant();
        return true;
    }

    @Override
    public Optional<JSONObject> fetchTokenData() {
        return Optional.empty();
    }

    public void revokeAllAccessTokensWithGivenGrant() {}
}
