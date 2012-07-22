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

package org.jongo.marshall.jackson.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bson.types.*;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

class JsonModule extends Module {

    @Override
    public String getModuleName() {
        return "JongoModule";
    }

    @Override
    public Version version() {
        return new Version(2, 0, 0, "", "org.jongo", "jongomodule");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new JsonSerializers());
        context.addDeserializers(new JsonDeserializers());
    }

    private static class JsonDeserializers extends SimpleDeserializers {
        public JsonDeserializers() {
            NativeDeserializer deserializer = new NativeDeserializer();
            addDeserializer(ObjectId.class, deserializer);
            addDeserializer(Date.class, deserializer);
            addDeserializer(UUID.class, deserializer);
            addDeserializer(Pattern.class, deserializer);
            addDeserializer(BSONTimestamp.class, deserializer);
            addDeserializer(MinKey.class, deserializer);
            addDeserializer(MaxKey.class, deserializer);
            addDeserializer(DBObject.class, deserializer);
        }
    }

    private static class JsonSerializers extends SimpleSerializers {
        public JsonSerializers() {
            NativeSerializer serializer = new NativeSerializer();
            addSerializer(ObjectId.class, serializer);
            addSerializer(Date.class, serializer);
            addSerializer(UUID.class, serializer);
            addSerializer(Pattern.class, serializer);
            addSerializer(BSONTimestamp.class, serializer);
            addSerializer(MinKey.class, serializer);
            addSerializer(MaxKey.class, serializer);
            addSerializer(DBObject.class, serializer);
        }
    }

    private static class NativeSerializer extends JsonSerializer<Object> {
        public void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeRawValue(JSON.serialize(obj));
        }
    }

    private static class NativeDeserializer<T> extends JsonDeserializer<T> {
        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String asString = jp.readValueAsTree().toString();
            return (T) JSON.parse(asString);
        }
    }
}
