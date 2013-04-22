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

import com.mongodb.*;
import org.bson.LazyBSONCallback;
import org.bson.types.ObjectId;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Marshaller;
import org.jongo.query.QueryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Insert {

    private final Marshaller marshaller;
    private final DBCollection collection;
    private final ObjectIdUpdater objectIdUpdater;
    private final QueryFactory queryFactory;
    private WriteConcern writeConcern;

    Insert(DBCollection collection, WriteConcern writeConcern, Marshaller marshaller, ObjectIdUpdater objectIdUpdater, QueryFactory queryFactory) {
        this.writeConcern = writeConcern;
        this.marshaller = marshaller;
        this.collection = collection;
        this.objectIdUpdater = objectIdUpdater;
        this.queryFactory = queryFactory;
    }

    public WriteResult save(Object pojo) {
        ObjectId id = preparePojo(pojo);
        return collection.save(convertToDBObject(pojo, id), writeConcern);
    }

    public WriteResult insert(Object... pojos) {
        List<DBObject> dbos = new ArrayList<DBObject>(pojos.length);
        for (Object pojo : pojos) {
            ObjectId id = preparePojo(pojo);
            dbos.add(convertToDBObject(pojo, id));
        }
        return collection.insert(dbos, writeConcern);
    }

    private ObjectId preparePojo(Object pojo) {
        if (objectIdUpdater.mustGenerateObjectId(pojo)) {
            ObjectId newOid = ObjectId.get();
            objectIdUpdater.setObjectId(pojo, newOid);
            return newOid;
        }
        return objectIdUpdater.getObjectId(pojo);
    }

    public WriteResult insert(String query, Object... parameters) {
        DBObject dbQuery = queryFactory.createQuery(query, parameters).toDBObject();
        return collection.insert(dbQuery, writeConcern);
    }

    private DBObject convertToDBObject(Object pojo, ObjectId id) {
        BsonDocument document = marshallDocument(pojo);
        DBObject dbo = new AlreadyCheckedDBObject(document.toByteArray(), id);
        dbo.put("_id", id);
        return dbo;
    }

    private BsonDocument marshallDocument(Object pojo) {
        try {
            return marshaller.marshall(pojo);
        } catch (Exception e) {
            String message = String.format("Unable to save object %s due to a marshalling error", pojo);
            throw new IllegalArgumentException(message, e);
        }
    }

    private final static class AlreadyCheckedDBObject extends LazyWriteableDBObject {

        private final Set<String> keys;
        private final ObjectId id;

        private AlreadyCheckedDBObject(byte[] data, ObjectId id) {
            super(data, new LazyBSONCallback());
            this.id = id;
            this.keys = new HashSet<String>();
        }

        @Override
        public Object get(String key) {
            if ("_id".equals(key)) {
                return id;
            }
            return super.get(key);
        }

        @Override
        public Set<String> keySet() {
            Set<String> combined = new HashSet<String>();
            combined.addAll(keys);
            combined.addAll(super.keySet());
            return combined;
        }
    }
}
