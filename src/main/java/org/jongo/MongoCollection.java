package org.jongo;

import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

public interface MongoCollection {
    FindOne findOne(String query);

    FindOne findOne(ObjectId id);

    FindOne findOne(String query, Object... parameters);

    Find find(String query);

    Find find(String query, Object... parameters);

    long count();

    long count(String query);

    long count(String query, Object... parameters);

    WriteResult update(String query, String modifier);

    WriteResult update(String query, String modifier, WriteConcern concern);

    WriteResult upsert(String query, String modifier);

    WriteResult upsert(String query, String modifier, WriteConcern concern);

    <D> String save(D document);

    <D> String save(D document, WriteConcern concern);

    WriteResult insert(String query);

    WriteResult insert(String query, Object... parameters);

    WriteResult remove(String query);

    WriteResult remove(String query, Object... parameters);

    WriteResult remove(ObjectId id);

    @SuppressWarnings("unchecked")
    <T> Iterable<T> distinct(String key, String query, Class<T> clazz);

    void drop();

    void ensureIndex(String index);

    String getName();

    DBCollection getDBCollection();
}
