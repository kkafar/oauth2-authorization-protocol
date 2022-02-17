package pl.edu.agh.dp.oauth2server.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDatabaseInstance {

    private MongoDatabaseInstance() {}

    public static MongoDatabase getDatabase() {
        return SingletonHelper.DATABASE;
    }

    private static class SingletonHelper {
        private static final String DATABASE_NAME = "res-server";
        private static final MongoDatabase DATABASE = MongoClients.create(getSettings()).getDatabase(DATABASE_NAME);

        private static MongoClientSettings getSettings() {
            ConnectionString connectionString = new ConnectionString("mongodb+srv://oauth2-res-server:oauth2-res-server@resourceserver.y1eji.mongodb.net/res-server?retryWrites=true&w=majority");

            CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
            CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    pojoCodecRegistry);

            return MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .codecRegistry(codecRegistry)
                    .build();
        }
    }
}
