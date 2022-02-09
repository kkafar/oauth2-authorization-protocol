package pl.edu.agh.dp.tkgk.oauth2server.database.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public final class MongoClientInstance {

    private static class SingletonHelper {

        private static MongoClientSettings getSettings() {
            CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
            CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    pojoCodecRegistry);
            ConnectionString connectionString =
                    new ConnectionString("mongodb+srv://auth-server-oauth2:auth-server-oauth2@cluster0.yvxcd.mongodb.net/auth-server?retryWrites=true&w=majority");
            return MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .codecRegistry(codecRegistry)
                    .build();
        }

        private static final MongoClient INSTANCE = MongoClients.create(getSettings());
    }

    private MongoClientInstance() { }

    public static MongoClient get() {
        return SingletonHelper.INSTANCE;
    }

    public static MongoDatabase getDatabase() { return get().getDatabase(MongoDBInfo.DATABASE_NAME); }
}

