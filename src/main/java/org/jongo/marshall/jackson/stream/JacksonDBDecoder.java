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
package org.jongo.marshall.jackson.stream;

import java.io.IOException;
import java.io.InputStream;

import org.bson.LazyBSONDecoder;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.jongo.marshall.jackson.JacksonProcessor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 *
 */
public class JacksonDBDecoder extends LazyBSONDecoder implements DBDecoder {

    public final static DBDecoderFactory FACTORY = new JacksonDBDecoderFactory();

    private final BsonFactory bsonFactory;

    public JacksonDBDecoder() {
        this.bsonFactory = new MongoBsonFactory(JacksonProcessor.createPreConfiguredMapper());
        bsonFactory.enable(BsonGenerator.Feature.ENABLE_STREAMING);
    }

    public DBCallback getDBCallback(DBCollection collection) {
        return new JacksonDBCallback(collection, bsonFactory);
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

    private static class MongoBsonFactory extends BsonFactory {

        private MongoBsonFactory(ObjectCodec oc) {
            super(oc);
        }

        @Override
        protected BsonParser _createJsonParser(InputStream in, IOContext ctxt) throws IOException, JsonParseException {
            BsonParser p = new MongoBsonParser(ctxt, _parserFeatures, _bsonParserFeatures, in);
            ObjectCodec codec = getCodec();
            if (codec != null) {
                p.setCodec(codec);
            }
            return p;
        }
    }

    private static class MongoBsonParser extends BsonParser {

        private MongoBsonParser(IOContext ctxt, int jsonFeatures, int bsonFeatures, InputStream in) {
            super(ctxt, jsonFeatures, bsonFeatures, in);
        }

        @Override
        public Object getEmbeddedObject() throws IOException, JsonParseException {
            Object object = super.getEmbeddedObject();
            if (object instanceof ObjectId) {
                return convertToNativeObjectId((ObjectId) object);
            }
            if (object instanceof Timestamp) {
                return convertToBSONTimestamp((Timestamp) object);
            }
            if (object instanceof JavaScript) {
                return convertToCode((JavaScript) object);
            }
            if ("MinKey".equals(object)) {
                return new MinKey();
            }
            if ("MaxKey".equals(object)) {
                return new MaxKey();
            }
            return object;
        }

        private Code convertToCode(JavaScript script) {
            return new Code(script.getCode());
        }

        private BSONTimestamp convertToBSONTimestamp(Timestamp ts) {
            return new BSONTimestamp(ts.getTime(), ts.getInc());
        }

        private org.bson.types.ObjectId convertToNativeObjectId(ObjectId id) {
            return new org.bson.types.ObjectId(id.getTime(), id.getMachine(), id.getInc());
        }
    }

    public static class JacksonDBDecoderFactory implements DBDecoderFactory {

        public DBDecoder create() {
            return new JacksonDBDecoder();
        }

    }
}
