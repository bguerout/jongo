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

package com.jongo;

import static com.jongo.MongoCollection.MONGO_ID;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoIterator<E> implements Iterator<E> {

    private final DBCursor cursor;
    private final Class<E> clazz;
    private final JsonMapper mapper;

    public MongoIterator(DBCursor cursor, Class<E> clazz) {
	this(cursor, clazz, new JsonMapper());
    }

    public MongoIterator(DBCursor cursor, Class<E> clazz, JsonMapper mapper) {
	this.clazz = clazz;
	this.cursor = cursor;
	this.mapper = mapper;
    }

    public boolean hasNext() {
	return cursor.hasNext();
    }

    public E next() {
	if (!hasNext())
	    throw new NoSuchElementException();

	try {
	    DBObject dbObject = cursor.next();
	    setIdProperly(dbObject);

	    String json = dbObject.toString();
	    return mapper.getEntity(json, clazz);
	} catch (IOException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    private void setIdProperly(DBObject dbObject) {
	Object id = dbObject.get(MONGO_ID);
	if (id != null)
	    dbObject.put(MONGO_ID, id.toString());
    }

    public void remove() {
	throw new UnsupportedOperationException("remove() method is not supported");
    }
}
