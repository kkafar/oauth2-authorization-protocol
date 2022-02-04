package pl.edu.agh.dp.tkgk.oauth2server.database.queries;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import java.util.List;

public class GenericQueries {

    public <T> void addObjectToCollection(T object, MongoCollection<T> objects) {
        objects.insertOne(object);
    }

    public <T> void addObjectsToCollection(List<T> newObjects, MongoCollection<T> objects) {
        objects.insertMany(newObjects);
    }

    public <T> void deleteObjectsFromCollection(MongoCollection<T> objects, Bson filter) {
        objects.deleteMany(filter);
    }

    public <T> void deleteObjectFromCollection(MongoCollection<T> objects, Bson filter) { objects.deleteOne(filter); }

    public <T> FindIterable<T> getObjectsFromCollection(MongoCollection<T> objects, Bson filter) {
        return objects.find(filter);
    }

    public <T> void updateObjectFromCollection(MongoCollection<T> objects, Bson filter, Bson update) {
        objects.updateOne(filter, update);
    }
}
