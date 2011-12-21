package com.jongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoCollection
{
    private static final String MONGO_ID = "_id";
    private DBCollection collection;

    public MongoCollection(String database, String collection) throws UnknownHostException, MongoException
    {
        this.collection = new Mongo().getDB(database).getCollection(collection);
    }

    public <T> T findOne(String query, Class<T> clazz)
    {
        return null;
    }

    public <T> Iterator<T> find(String query, Class<T> clazz)
    {
        DBCursor cursor = collection.find(DBObjectConvertor.from(query));
        return new MongoIterator<T>(cursor, clazz);
    }

    public <D> void save(D document) throws IOException
    {
        collection.save(Jongo.marshall(document));
    }

    public void drop()
    {
        collection.drop();
    }
}
