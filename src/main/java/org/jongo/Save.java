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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;

import static org.jongo.MongoCollection.MONGO_DOCUMENT_ID_NAME;

class Save {

    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    private final DBCollection collection;
    private final Object document;
    private WriteConcern concern;

    Save(DBCollection collection, Marshaller marshaller, Unmarshaller unmarshaller, Object document) {
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.document = document;
    }

    public Save concern(WriteConcern concern) {
        this.concern = concern;
        return this;
    }

    public WriteResult execute() {
        DBObject dbObject = marshall();

        WriteResult writeResult = collection.save(dbObject, determineWriteConcern());

        String id = dbObject.get(MONGO_DOCUMENT_ID_NAME).toString();
        unmarshaller.setDocumentGeneratedId(document, id);

        return writeResult;
    }

    private DBObject marshall() {
        try {
            return marshaller.marshallAsBson(document);
        } catch (Exception e) {
            String message = String.format("Unable to save object %s due to a marshalling error", document);
            throw new IllegalArgumentException(message, e);
        }
    }

    private WriteConcern determineWriteConcern() {
        return concern == null ? collection.getWriteConcern() : concern;
    }

    private DBObject convertToJson(String json) {
        try {
            return ((DBObject) JSON.parse(json));
        } catch (Exception e) {
            String message = String.format("Unable to save document, " + "json returned by marshaller cannot be converted into a DBObject: '%s'", json);
            throw new IllegalArgumentException(message, e);
        }
    }
}
