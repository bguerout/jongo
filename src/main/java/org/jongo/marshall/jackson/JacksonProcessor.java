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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper mapper;
    private final BsonFactory bsonFactory;

    public JacksonProcessor() {
        this(createPreConfiguredMapper());
    }

    public JacksonProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
        this.bsonFactory = new MongoBsonFactory(mapper);
        bsonFactory.enable(BsonGenerator.Feature.ENABLE_STREAMING);
    }

    public <T> T unmarshall(byte[] data, int offset, Class<T> clazz) throws MarshallingException {

        try {
            JsonParser bsonParser = bsonFactory.createJsonParser(data, offset, data.length - offset);
            return mapper.readValue(bsonParser, clazz);
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result into %s", clazz);
            throw new MarshallingException(message, e);
        }
    }

    public <T> String marshall(T obj) throws MarshallingException {
        try {
            Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return writer.toString();
        } catch (Exception e) {
            String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

    public static ObjectMapper createPreConfiguredMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);
        mapper.configure(AUTO_DETECT_SETTERS, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
        mapper.registerModule(new BsonModule());
        return mapper;
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

}
