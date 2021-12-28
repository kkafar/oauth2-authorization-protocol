package pl.edu.agh.dp.tkgk.oauth2server.database;

import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.util.Optional;

public interface Database {

    Optional<Client> getClient(String clientId);

}
