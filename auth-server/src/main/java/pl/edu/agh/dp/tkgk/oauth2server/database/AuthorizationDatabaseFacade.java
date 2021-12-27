package pl.edu.agh.dp.tkgk.oauth2server.database;

class AuthorizationDatabaseFacade implements Database{

    private AuthorizationDatabaseFacade(){}

    private static class AuthorizationDatabaseFacadeHolder{
        private static final AuthorizationDatabaseFacade database = new AuthorizationDatabaseFacade();
    }

    public static AuthorizationDatabaseFacade getInstance(){
        return AuthorizationDatabaseFacadeHolder.database;
    }
}
