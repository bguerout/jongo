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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.bson.types.*;

import java.io.IOException;

class BsonSerializers extends SimpleSerializers {
    public BsonSerializers() {
        addSerializer(org.bson.types.ObjectId.class, new BsonObjectIdSerializer());
        addSerializer(BSONTimestamp.class, new BSONTimestampSerializer());
        addSerializer(MinKey.class, new MinKeySerializer());
        addSerializer(MaxKey.class, new MaxKeySerializer());
        addSerializer(Binary.class, new BinarySerializer());
    }

    static class MaxKeySerializer extends JsonSerializer<MaxKey> {

        public void serialize(MaxKey obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            ((MongoBsonGenerator) jsonGenerator).writeMaxKey(obj);
        }
    }

    static class MinKeySerializer extends JsonSerializer<MinKey> {

        public void serialize(MinKey obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            ((MongoBsonGenerator) jsonGenerator).writeMinKey(obj);
        }
    }

    static class BSONTimestampSerializer extends JsonSerializer<BSONTimestamp> {

        public void serialize(BSONTimestamp obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            ((MongoBsonGenerator) jsonGenerator).writeBSONTimestamp(obj);
        }
    }

    static class BsonObjectIdSerializer extends JsonSerializer<ObjectId> {

        public void serialize(ObjectId obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            ((MongoBsonGenerator) jsonGenerator).writeNativeObjectId(obj);
        }
    }

    static class BinarySerializer extends JsonSerializer<Binary> {

        public void serialize(Binary obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            ((MongoBsonGenerator) jsonGenerator).writeBinary(obj);
        }
    }

}
