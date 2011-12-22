package com.jongo;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;

public class MongoCollection {

    private final DBCollection collection;
    private final DBObjectConvertor convertor;

    public MongoCollection(String database, String collection) throws UnknownHostException, MongoException {
        this.collection = new Mongo().getDB(database).getCollection(collection);
        this.convertor = new DBObjectConvertor();
    }

    public <T> Iterator<T> find(String query, Class<T> clazz) {
        DBCursor cursor = collection.find(convertor.convert(query));
        return new MongoIterator<T>(cursor, clazz);
    }

    public <D> void save(D document) throws IOException {
        collection.save(convertor.convert(document));
    }

    public void drop() {
        collection.drop();
    }
}
