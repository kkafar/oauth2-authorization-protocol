package pl.edu.agh.dp.tkgk.oauth2server.database;

import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.util.Optional;

class AuthorizationDatabaseFacade implements Database{

    private AuthorizationDatabaseFacade(){}

    @Override
    public Optional<Client> getClient(String clientId) {
        return null;
    }
    public boolean revokeAccessToken() { return true; }

    @Override
    public boolean revokeRefreshToken() { return true; }

    private static class AuthorizationDatabaseFacadeHolder{
        private static final AuthorizationDatabaseFacade database = new AuthorizationDatabaseFacade();
    }

    public static AuthorizationDatabaseFacade getInstance(){
        return AuthorizationDatabaseFacadeHolder.database;
    }
}
