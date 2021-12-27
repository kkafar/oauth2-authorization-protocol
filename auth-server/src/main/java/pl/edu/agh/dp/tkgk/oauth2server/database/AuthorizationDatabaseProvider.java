package pl.edu.agh.dp.tkgk.oauth2server.database;

public class AuthorizationDatabaseProvider {

    public static Database getInstance(){
        return MockAuthorizationDatabaseFacade.getInstance();
    }

}
