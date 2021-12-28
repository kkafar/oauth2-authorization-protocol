package pl.edu.agh.dp.tkgk.oauth2server.database;

import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.util.Collections;
import java.util.Optional;

class MockAuthorizationDatabaseFacade implements Database{

    private MockAuthorizationDatabaseFacade(){}

    @Override
    public Optional<Client> getClient(String clientId) {
        if("some_fake_client_id".equals(clientId)){
            Client fakeClient = new Client("some_fake_client_id", "https://www.google.pl", Collections.singletonList("all"));
            return Optional.of(fakeClient);
        }
        return Optional.empty();
    }

    private static class MockAuthorizationDatabaseFacadeHolder{
        public static  final MockAuthorizationDatabaseFacade database = new MockAuthorizationDatabaseFacade();
    }

    public static MockAuthorizationDatabaseFacade getInstance(){
        return MockAuthorizationDatabaseFacadeHolder.database;
    }

}
