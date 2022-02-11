package pl.edu.agh.dp.oauth2server.database;

import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import pl.edu.agh.dp.oauth2server.model.User;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDatabaseFacade {
    private static final MongoDatabase DATABASE = MongoDatabaseInstance.getDatabase();
    private static final String COLLECTION_NAME = "user_data";

    public static String getUsersUsername (String userID) {
        User user = getUser(userID);
        return user.getUsername();
    }

    public static String getUsersMail (String userID) {
        User user = getUser(userID);
        return user.getMail();
    }

    public static List<String> getUsersPosts (String userID) {
        User user = getUser(userID);
        return user.getPosts();
    }

    private static User getUser(String userID) {
        Bson filter = eq("user_id", userID);
        return DATABASE.getCollection(COLLECTION_NAME, User.class).find(filter).first();
    }
}
