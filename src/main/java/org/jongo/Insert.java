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
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Marshaller;
import org.jongo.query.QueryFactory;

import java.util.ArrayList;
import java.util.List;

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
        Object id = preparePojo(pojo);
        DBObject dbo = convertToDBObject(pojo, id);
        return collection.save(dbo, writeConcern);
    }

    public WriteResult insert(Object... pojos) {
        List<DBObject> dbos = new ArrayList<DBObject>(pojos.length);
        for (Object pojo : pojos) {
            Object id = preparePojo(pojo);
            DBObject dbo = convertToDBObject(pojo, id);
            dbos.add(dbo);
        }
        return collection.insert(dbos, writeConcern);
    }

    public WriteResult insert(String query, Object... parameters) {
        DBObject dbQuery = queryFactory.createQuery(query, parameters).toDBObject();
        if (dbQuery instanceof BasicDBList) {
            return insert(((BasicDBList) dbQuery).toArray());
        } else {
            return collection.insert(dbQuery, writeConcern);
        }
    }

    private Object preparePojo(Object pojo) {
        if (objectIdUpdater.mustGenerateObjectId(pojo)) {
            ObjectId newOid = ObjectId.get();
            objectIdUpdater.setObjectId(pojo, newOid);
            return newOid;
        }
        return objectIdUpdater.getId(pojo);
    }

    private DBObject convertToDBObject(Object pojo, Object id) {
        BsonDocument document = asBsonDocument(marshaller, pojo);
        return new LazyIdDBObject(document.toByteArray(), marshaller, id);
    }

    private final static class LazyIdDBObject extends LazyWriteableDBObject {

        private final Object id;
        private final Marshaller marshaller;

        private LazyIdDBObject(byte[] data, Marshaller marshaller, Object id) {
            super(data, new LazyBSONCallback());
            this.marshaller = marshaller;
            this.id = id;
        }

        @Override
        public Object get(String key) {
            if ("_id".equals(key) && id != null) {
                if (Bson.isPrimitive(id)) {
                    return id;
                } else {
                    return asBsonDocument(marshaller, id).toDBObject();
                }
            }
            return super.get(key);
        }
    }

    private static BsonDocument asBsonDocument(Marshaller marshaller, Object obj) {
        try {
            return marshaller.marshall(obj);
        } catch (Exception e) {
            String message = String.format("Unable to save object %s due to a marshalling error", obj);
            throw new IllegalArgumentException(message, e);
        }
    }
}
