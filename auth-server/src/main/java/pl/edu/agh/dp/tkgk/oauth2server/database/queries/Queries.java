package pl.edu.agh.dp.tkgk.oauth2server.database.queries;

import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Converts methods' parameters to Bson objects and then uses them with <code>GenericQueries</code> class' methods
 * to execute database queries indirectly.
 */
public class Queries {

    private final GenericQueries genericQueries = new GenericQueries();

    public <T> void addObjectToCollection(T object, MongoCollection<T> objects) {
        genericQueries.addObjectToCollection(object, objects);
    }

    public <T> void addObjectsToCollection(List<T> newObjects, MongoCollection<T> objects) {
        genericQueries.addObjectsToCollection(newObjects, objects);
    }

    public <T> boolean deleteObjectsFromCollection(MongoCollection<T> objects, String fieldName, String fieldValue) {
        Bson filter = eq(fieldName, fieldValue);
        return genericQueries.deleteObjectsFromCollection(objects, filter);
    }

    public <T> boolean deleteObjectFromCollection(MongoCollection<T> objects, String fieldName, String fieldValue) {
        Bson filter = eq(fieldName, fieldValue);
        return genericQueries.deleteObjectFromCollection(objects, filter);
    }

    public <T, K> boolean updateObjectFromCollection(MongoCollection<T> objects, String fieldName, K fieldValue,
                                                  String updateFieldName, K updateValue)
    {
        Bson filter = eq(fieldName, fieldValue);
        Bson update = set(updateFieldName, updateValue);
        return genericQueries.updateObjectFromCollection(objects, filter, update);
    }

    public <T, K> T getObjectFromCollection(MongoCollection<T> objects, String fieldName, K fieldValue) {
        Bson filter = eq(fieldName, fieldValue);
        return genericQueries.getObjectsFromCollection(objects, filter).first();
    }

    public <T, K> List<T> getObjectsFromCollection(MongoCollection<T> objects, String fieldName, K fieldValue) {
        if (fieldValue == null) return objects.find().into(new ArrayList<T>());
        Bson filter = eq(fieldName, fieldValue);
        return genericQueries.getObjectsFromCollection(objects, filter).into(new ArrayList<T>());
    }
}
