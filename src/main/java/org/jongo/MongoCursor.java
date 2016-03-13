/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoIterable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

//TODO I believe this class is now obsolete: the MongoDriver implements Iterable already
public class MongoCursor<E> implements Iterator<E>, Iterable<E>, Closeable {

    private final MongoIterable<BasicDBObject> cursor;
    private final ResultHandler<E> resultHandler;

    public MongoCursor(MongoIterable<BasicDBObject> cursor, ResultHandler<E> resultHandler) {
        this.cursor = cursor;
        this.resultHandler = resultHandler;
    }

    public boolean hasNext() {
        return cursor.iterator().hasNext();
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        DBObject dbObject = cursor.iterator().next();
        return resultHandler.map(dbObject);
    }

    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }

    public Iterator<E> iterator() {
        return new MongoCursor<E>(cursor, resultHandler);
    }

    public void close() throws IOException {
        //TODO does it need to close?
    }

    //TODO does it make sense anymore?
    public int count() {
        return 0;
    }
}
