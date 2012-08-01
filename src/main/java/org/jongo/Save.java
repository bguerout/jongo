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
import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;

class Save {

    private final Marshaller marshaller;
    private final DBCollection collection;
    private final Object document;
    private WriteConcern concern;

    Save(DBCollection collection, Marshaller marshaller, Object document) {
        this.marshaller = marshaller;
        this.collection = collection;
        this.document = document;
    }

    public Save concern(WriteConcern concern) {
        this.concern = concern;
        return this;
    }

    public WriteResult execute() {
        marshaller.setDocumentGeneratedId(document, ObjectId.get());
        return collection.save(marshallDocument(), determineWriteConcern());
    }

    private DBObject marshallDocument() {
        try {
            return marshaller.marshall(document);
        } catch (Exception e) {
            String message = String.format("Unable to save object %s due to a marshalling error", document);
            throw new IllegalArgumentException(message, e);
        }
    }

    private WriteConcern determineWriteConcern() {
        return concern == null ? collection.getWriteConcern() : concern;
    }
}
