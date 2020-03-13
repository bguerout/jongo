/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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
import com.mongodb.gridfs.GridFSDBFile;

import java.util.Iterator;

public class BsonDBDecoder extends LazyDBDecoder implements DBDecoder {

    public final static DBDecoderFactory FACTORY = new BsonDBDecoderFactory();

    private BsonDBDecoder() {
    }

    public DBCallback getDBCallback(DBCollection collection) {
        return new CollectionDBCallback(collection);
    }

    private static class BsonDBDecoderFactory implements DBDecoderFactory {

        public DBDecoder create() {
            return new BsonDBDecoder();
        }
    }

    private static class CollectionDBCallback extends LazyDBCallback {

        private final DBCollection collection;

        public CollectionDBCallback(DBCollection collection) {
            super(collection);
            this.collection = collection;
        }

        @Override
        public Object createObject(byte[] data, int offset) {

            if (isGridFSCollection()) {
                return DefaultDBDecoder.FACTORY.create().decode(data, collection);
            }

            DBObject dbo = new BsonDBObject(data, offset);
            if (isDBRef(dbo)) {
                return new DBRef((String) dbo.get("$ref"), dbo.get("$id"));
            }
            return dbo;
        }

        private boolean isGridFSCollection() {
            return GridFSDBFile.class.equals(collection.getObjectClass());
        }

        private boolean isDBRef(DBObject dbo) {
            Iterator<String> iterator = dbo.keySet().iterator();
            return iterator.hasNext() && iterator.next().equals("$ref") && iterator.hasNext() && iterator.next().equals("$id");
        }

    }

}
