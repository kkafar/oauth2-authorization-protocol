package pl.edu.agh.dp.tkgk.oauth2server.database.queries;

import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

/**
 * Converts methods' parameters to Bson objects and then uses them with <code>GenericQueries</code> class' methods
 * to execute database queries.
 */
public class Queries {

    private final GenericQueries genericQueries = new GenericQueries();

    public <T> void addObjectToCollection(T object, MongoCollection<T> objects) {
        genericQueries.addObjectToCollection(object, objects);
    }

    public <T, K> void deleteObjectsFromCollection(MongoCollection<T> objects, String fieldName, String fieldValue) {
        Bson filter = eq(fieldName, fieldValue);
        genericQueries.deleteObjectsFromCollection(objects, filter);
    }

    public <T, K> void deleteObjectFromCollection(MongoCollection<T> objects, String fieldName, String fieldValue) {
        Bson filter = eq(fieldName, fieldValue);
        genericQueries.deleteObjectFromCollection(objects, filter);
    }

    public <T, K> void updateObjectFromCollection(MongoCollection<T> objects, String fieldName, K fieldValue, K updateValue) {
        Bson filter = eq(fieldName, fieldValue);
        Bson update = eq(fieldName, updateValue);
        genericQueries.updateObjectFromCollection(objects, filter, update);
    }

    public <T, K> T getObjectFromCollection(MongoCollection<T> objects, String fieldName, K fieldValue) {
        Bson filter = eq(fieldName, fieldValue);
        return genericQueries.getObjectsFromCollection(objects, filter).first();
    }

}
