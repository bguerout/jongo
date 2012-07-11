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
package org.jongo.marshall.jackson.stream;

import org.bson.LazyBSONCallback;
import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBRef;
import com.mongodb.LazyDBCallback;
import de.undercouch.bson4jackson.BsonFactory;

/**
 *
 */
public class JacksonDBCallback extends LazyBSONCallback implements DBCallback {

    private final DBCollection collection;
    private final DB db;
    private final BsonFactory bsonFactory;

    public JacksonDBCallback(DBCollection coll,BsonFactory bsonFactory) {
        collection = coll;
        db = collection == null ? null : collection.getDB();
        this.bsonFactory = bsonFactory;
    }

    @Override
    public Object createObject(byte[] data, int offset) {
        return new LazyJacksonDBObject(data, offset,new LazyDBCallback(collection),bsonFactory);
    }

    @Override
    public Object createDBRef(String ns, ObjectId id) {
        return new DBRef(db, ns, id);
    }

}
