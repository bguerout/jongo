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

package org.jongo.bson;

import com.mongodb.*;
import org.bson.LazyBSONCallback;

import java.util.Iterator;

public class BsonDBDecoder extends LazyDBDecoder implements DBDecoder {

    public final static DBDecoderFactory FACTORY = new BsonDBDecoderFactory();

    private BsonDBDecoder() {
    }

    public DBCallback getDBCallback(DBCollection collection) {
        return new BsonDBCallback(collection);
    }

    private static class BsonDBDecoderFactory implements DBDecoderFactory {

        public DBDecoder create() {
            return new BsonDBDecoder();
        }
    }

    private static class BsonDBCallback extends LazyDBCallback {

        private final DB db;

        public BsonDBCallback(DBCollection collection) {
            super(collection);
            this.db = collection == null ? null : collection.getDB();
        }

        @Override
        public Object createObject(byte[] data, int offset) {
            DBObject dbo = new RelaxedLazyDBObject(data, new LazyBSONCallback());

            Iterator it = dbo.keySet().iterator();
            if (it.hasNext() && it.next().equals("$ref") && dbo.containsField("$id")) {
                return new DBRef(db, dbo);
            }
            return dbo;
        }

    }

}
