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

package org.jongo.marshall.decoder;

import com.mongodb.*;
import org.bson.LazyBSONCallback;
import org.bson.types.ObjectId;
import org.jongo.marshall.Unmarshaller;

import java.util.Iterator;

class LazyDocumentCallback extends LazyBSONCallback implements DBCallback {

    private final DBCollection collection;
    private final DB db;
    private final Unmarshaller unmarshaller;

    public LazyDocumentCallback(DBCollection collection, Unmarshaller unmarshaller) {
        this.collection = collection;
        this.db = collection == null ? null : collection.getDB();
        this.unmarshaller = unmarshaller;
    }

    @Override
    public Object createObject(byte[] data, int offset) {
        LazyDocumentStream o = new LazyDocumentStream(data, offset, new LazyDBCallback(collection), unmarshaller);

        Iterator it = o.keySet().iterator();
        if (it.hasNext() && it.next().equals("$ref") &&
                o.containsField("$id")) {
            return new DBRef(db, o);
        }
        return o;
    }

    public Object createDBRef(String ns, ObjectId id) {
        return new DBRef(db, ns, id);
    }
}
