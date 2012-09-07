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

package org.jongo.marshall.stream;

import com.mongodb.*;
import org.bson.LazyBSONDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BeanDecoder extends LazyBSONDecoder implements DBDecoder {

    public final static DBDecoderFactory FACTORY = new BeanDecoderFactory();

    public DBCallback getDBCallback(DBCollection collection) {
        return new BeanDecoderCallback(collection);
    }

    public DBObject decode(byte[] b, DBCollection collection) {
        DBCallback cbk = getDBCallback(collection);
        cbk.reset();
        decode(b, cbk);
        return (DBObject) cbk.get();
    }

    public DBObject decode(InputStream in, DBCollection collection) throws IOException {
        DBCallback cbk = getDBCallback(collection);
        cbk.reset();
        decode(in, cbk);
        return (DBObject) cbk.get();
    }

    private BeanDecoder() {
    }


    private static class BeanDecoderFactory implements DBDecoderFactory {

        public DBDecoder create() {
            return new BeanDecoder();
        }
    }

    private static class BeanDecoderCallback extends LazyDBCallback  {

        private final DB db;

        public BeanDecoderCallback(DBCollection collection) {
            super(collection);
            this.db = collection == null ? null : collection.getDB();
        }

        @Override
        public Object createObject(byte[] data, int offset) {
            DBObject dbo = new ByteArrayBsonStream(data, offset, this);

            Iterator it = dbo.keySet().iterator();
            if (it.hasNext() && it.next().equals("$ref") && dbo.containsField("$id")) {
                return new DBRef(db, dbo);
            }
            return dbo;
        }
    }
}
