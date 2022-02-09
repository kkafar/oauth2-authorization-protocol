package pl.edu.agh.dp.tkgk.oauth2server.common;

import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

public interface DatabaseInjectable {
    void setDatabase(Database database);
}
