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

import java.util.HashSet;
import java.util.Set;

class Save {

    private final Marshaller marshaller;
    private final DBCollection collection;
    private final ObjectIdUpdater objectIdUpdater;
    private final Object pojo;
    private WriteConcern writeConcern;

    Save(DBCollection collection, WriteConcern writeConcern, Marshaller marshaller, ObjectIdUpdater objectIdUpdater, Object pojo) {
        this.writeConcern = writeConcern;
        this.marshaller = marshaller;
        this.collection = collection;
        this.objectIdUpdater = objectIdUpdater;
        this.pojo = pojo;
    }

    public WriteResult execute() {
        DBObject dbObject;
        if (objectIdUpdater.canSetObjectId(pojo)) {
            dbObject = createDBObjectToInsert();
        } else {
            dbObject = createDBObjectToUpdate();
        }

        return collection.save(dbObject, writeConcern);
    }

    private DBObject createDBObjectToUpdate() {
        BsonDocument document = marshallDocument();
        return new AlreadyCheckedDBObject(document.toByteArray());
    }

    private DBObject createDBObjectToInsert() {
        ObjectId id = ObjectId.get();
        objectIdUpdater.setDocumentGeneratedId(pojo, id);

        BsonDocument document = marshallDocument();
        DBObject dbo = new AlreadyCheckedDBObject(document.toByteArray());
        dbo.put("_id", id);

        return dbo;
    }

    private BsonDocument marshallDocument() {
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
