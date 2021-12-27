package pl.edu.agh.dp.tkgk.oauth2server.database;

class MockAuthorizationDatabaseFacade implements Database{

    private MockAuthorizationDatabaseFacade(){}

    private static class MockAuthorizationDatabaseFacadeHolder{
        public static  final MockAuthorizationDatabaseFacade database = new MockAuthorizationDatabaseFacade();
    }

    public static MockAuthorizationDatabaseFacade getInstance(){
        return MockAuthorizationDatabaseFacadeHolder.database;
    }

}
