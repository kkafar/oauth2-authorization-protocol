package pl.edu.agh.dp.tkgk.oauth2server.database.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoClientInstance {

    private static class SingletonHelper {

        private static MongoClientSettings getSettings() {
            ConnectionString connectionString =
                    new ConnectionString("mongodb+srv://auth-server-oauth2:auth-server-oauth2@cluster0.yvxcd.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
            return MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
        }

        private static final MongoClient INSTANCE = MongoClients.create(getSettings());
    }

    private MongoClientInstance() { }

    public static MongoClient get() {
        return SingletonHelper.INSTANCE;
    }
}

