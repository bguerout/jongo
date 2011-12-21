package com.jongo;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.mongodb.DBCursor;

public class MongoIterator<E> implements Iterator<E>
{
    private DBCursor cursor;
    private Class<E> clazz;

    public MongoIterator(DBCursor cursor, Class<E> clazz)
    {
        this.cursor = cursor;
        this.clazz = clazz;
    }

    public boolean hasNext()
    {
        return cursor.hasNext();
    }

    public E next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        try
        {
            return Jongo.unmarshallString(cursor.next().toString(), clazz);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove() method is not supported");
    }
}
