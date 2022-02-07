package pl.edu.agh.dp.tkgk.oauth2server.database.queries;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * Executes database queries directly
 */
public class GenericQueries {

    public <T> void addObjectToCollection(T object, MongoCollection<T> objects) {
        objects.insertOne(object);
    }

    public <T> void addObjectsToCollection(List<T> newObjects, MongoCollection<T> objects) {
        objects.insertMany(newObjects);
    }

    public <T> boolean deleteObjectsFromCollection(MongoCollection<T> objects, Bson filter) {
        return objects.deleteMany(filter).getDeletedCount() != 0;
    }

    public <T> boolean deleteObjectFromCollection(MongoCollection<T> objects, Bson filter) { return objects.deleteOne(filter).getDeletedCount() != 0; }

    public <T> FindIterable<T> getObjectsFromCollection(MongoCollection<T> objects, Bson filter) {
        return objects.find(filter);
    }

    public <T> boolean updateObjectFromCollection(MongoCollection<T> objects, Bson filter, Bson update) {
        return objects.updateOne(filter, update).getModifiedCount() != 0;
    }
}
