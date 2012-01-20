package com.jongo;

import java.util.Iterator;

import com.jongo.jackson.EntityProcessor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class Queryings {

    EntityProcessor processor;
    DBCollection collection;

    Query query;

    public Queryings(EntityProcessor processor, DBCollection collection, String query) {
        this.processor = processor;
        this.collection = collection;
        this.query = Query.query(query);
    }

    public Queryings(EntityProcessor processor, DBCollection collection, String query, Object... parameters) {
        this.processor = processor;
        this.collection = collection;
        this.query = Query.query(query, parameters);
    }

    public Queryings on(String fields) {
        this.query = new Query.Builder(query.getQuery()).fields(fields).build();
        return this;
    }

    public <T> Iterator<T> as(Class<T> clazz) {
        return map(processor.createEntityMapper(clazz));
    }

    public <T> Iterator<T> map(DBObjectMapper<T> mapper) {
        DBCursor cursor = collection.find(query.toDBObject());
        return new MongoIterator<T>(cursor, mapper);
    }
}
