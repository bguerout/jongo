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

package org.jongo.marshall.jackson.bson4jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bson.types.Binary;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;

import java.io.IOException;
import java.util.Date;

class BsonDeserializers extends SimpleDeserializers {

    public BsonDeserializers() {
        addDeserializer(Date.class, new DateDeserializer());
        addDeserializer(MinKey.class, new MinKeyDeserializer());
        addDeserializer(MaxKey.class, new MaxKeyDeserializer());
        addDeserializer(Binary.class, new BinaryDeserializer());
        addDeserializer(DBObject.class, new NativeDeserializer());
    }

    private static class DateDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object deserialized = jp.getEmbeddedObject();
            if (deserialized instanceof Long) {
                return getDateFromBackwardFormat((Long) deserialized);
            }
            return (Date) deserialized;
        }

        private Date getDateFromBackwardFormat(Long deserialized) {
            return new Date(deserialized);
        }
    }

    private static class MinKeyDeserializer extends JsonDeserializer<MinKey> {
        @Override
        public MinKey deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return new MinKey();
        }
    }

    private static class MaxKeyDeserializer extends JsonDeserializer<MaxKey> {
        @Override
        public MaxKey deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return new MaxKey();
        }
    }

    private static class NativeDeserializer<T> extends JsonDeserializer<T> {
        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String asString = jp.readValueAsTree().toString();
            return (T) JSON.parse(asString);
        }
    }

    private static class BinaryDeserializer extends JsonDeserializer<Binary> {
        @Override
        public Binary deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object object = jp.getEmbeddedObject();
            return new Binary((byte[]) object);
        }
    }
}
