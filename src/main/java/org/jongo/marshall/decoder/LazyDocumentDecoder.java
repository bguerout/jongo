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

import java.io.IOException;
import java.io.InputStream;

import org.bson.LazyBSONDecoder;
import org.jongo.marshall.Unmarshaller;

import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;

public class LazyDocumentDecoder extends LazyBSONDecoder implements DBDecoder {

    public final static LazyDocumentDecoderFactory FACTORY = new LazyDocumentDecoderFactory();

    private LazyDocumentDecoder() {
    }

    public DBCallback getDBCallback(DBCollection collection) {
        return new LazyDocumentCallback(collection);
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


    private static class LazyDocumentDecoderFactory implements DBDecoderFactory {

        public DBDecoder create() {
            return new LazyDocumentDecoder();
        }

    }
}
