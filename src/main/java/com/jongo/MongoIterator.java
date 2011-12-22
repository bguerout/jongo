package com.jongo;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MongoIterator<E> implements Iterator<E> {
    private final DBCursor cursor;
    private final Class<E> clazz;
    private final JsonMapper mapper;

    public MongoIterator(DBCursor cursor, Class<E> clazz) {
        this.cursor = cursor;
        this.clazz = clazz;
        this.mapper = new JsonMapper();
    }

    public boolean hasNext() {
        return cursor.hasNext();
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        try {
            DBObject dbObject = cursor.next();
            String json = dbObject.toString();
            return mapper.getEntity(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }
}
