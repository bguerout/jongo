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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PojoUpdate {

    private final Marshaller marshaller;
    private final DBCollection collection;
    private final ObjectIdUpdater objectIdUpdater;
    private WriteConcern writeConcern;

    PojoUpdate(DBCollection collection, WriteConcern writeConcern, Marshaller marshaller, ObjectIdUpdater objectIdUpdater) {
        this.writeConcern = writeConcern;
        this.marshaller = marshaller;
        this.collection = collection;
        this.objectIdUpdater = objectIdUpdater;
    }

    public WriteResult save(Object pojo) {
        DBObject dbo;
        if (objectIdUpdater.canSetObjectId(pojo)) {
            dbo = createDBObjectToInsert(pojo);
        } else {
            dbo = createDBObjectToUpdate(pojo);
        }

        return collection.save(dbo, writeConcern);
    }

    public WriteResult insert(Object... pojos) {
        List<DBObject> dbos = new ArrayList<DBObject>(pojos.length);
        for (Object pojo : pojos) {
            if (!objectIdUpdater.canSetObjectId(pojo)) {
                throw new IllegalArgumentException("Unable to insert pojo with Id. Use save() method instead.");
            }
            dbos.add(createDBObjectToInsert(pojo));
        }
        return collection.insert(dbos, writeConcern);
    }

    private DBObject createDBObjectToUpdate(Object pojo) {
        BsonDocument document = marshallDocument(pojo);
        return new AlreadyCheckedDBObject(document.toByteArray());
    }

    private DBObject createDBObjectToInsert(Object pojo) {
        ObjectId id = ObjectId.get();
        objectIdUpdater.setObjectId(pojo, id);

        BsonDocument document = marshallDocument(pojo);
        DBObject dbo = new AlreadyCheckedDBObject(document.toByteArray());
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

        private AlreadyCheckedDBObject(byte[] data) {
            super(data, new LazyBSONCallback());
            this.keys = new HashSet<String>();
        }

        @Override
        public Set<String> keySet() {
            return keys;
        }
    }
}
