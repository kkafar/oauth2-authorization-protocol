package pl.edu.agh.dp.tkgk.oauth2server.database;

import org.json.JSONObject;
import java.util.Optional;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;


class AuthorizationDatabaseFacade implements Database{

    private AuthorizationDatabaseFacade(){}

    @Override
    public Optional<Client> getClient(String clientId) {
        return Optional.empty();
    }
    public boolean revokeAccessToken() { return true; }

    @Override
    public boolean revokeRefreshToken() { return true; }

    @Override
    public Optional<JSONObject> fetchTokenData() {
        return Optional.empty();
    }

    private static class AuthorizationDatabaseFacadeHolder{
        private static final AuthorizationDatabaseFacade database = new AuthorizationDatabaseFacade();
    }

    public static AuthorizationDatabaseFacade getInstance(){
        return AuthorizationDatabaseFacadeHolder.database;
    }
}
