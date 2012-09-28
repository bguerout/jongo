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

package org.jongo.marshall.jackson.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.jongo.bson.BsonByte;
import org.jongo.bson.BsonByteFactory;
import org.jongo.model.Friend;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.marshall.jackson.configuration.MappingConfigBuilder.usingJson;
import static org.jongo.marshall.jackson.configuration.MappingConfigBuilder.usingStream;

public class MappingConfigBuilderTest {

    @Test
    public void canAddDeserializer() throws Exception {

        MappingConfig config = usingJson()
                .addDeserializer(String.class, new DoeJsonDeserializer())
                .build();

        ObjectMapper mapper = config.getObjectMapper();

        Friend friend = mapper.readValue("{\"name\":\"robert\"}", Friend.class);
        assertThat(friend.getName()).isEqualTo("Doe");
    }

    @Test
    public void canAddSerializer() throws Exception {

        MappingConfig conf = usingJson()
                .addSerializer(String.class, new DoeJsonSerializer())
                .build();

        ObjectMapper mapper = conf.getObjectMapper();

        String friend = mapper.writeValueAsString(new Friend("Robert"));
        assertThat(friend).contains("\"name\":\"Doe\"");
    }

    @Test
    public void canAddModule() throws Exception {

        MappingConfig config = usingJson()
                .add(new JsonModule())
                .build();

        ObjectMapper mapper = config.getObjectMapper();

        ObjectId oid = new ObjectId("504482e5e4b0d1b2c47fff66");
        String robert = mapper.writeValueAsString(new Friend(oid, "Robert"));
        assertThat(robert).contains("\"_id\":{ \"$oid\" : \"504482e5e4b0d1b2c47fff66\"}");
    }

    @Test
    public void canCreateJsonMapper() throws Exception {

        MappingConfig config = usingJson()
                .addDeserializer(String.class, new DoeJsonDeserializer())
                .build();

        ObjectMapper mapper = config.getObjectMapper();

        ObjectId oid = new ObjectId("504482e5e4b0d1b2c47fff66");
        String robert = mapper.writeValueAsString(new Friend(oid, "Robert"));
        assertThat(robert).contains("\"_id\":{ \"$oid\" : \"504482e5e4b0d1b2c47fff66\"}");
    }

    @Test
    public void canCreateConfigWithCustomMapper() throws Exception {

        MappingConfig config = new MappingConfigBuilder(new ObjectMapper())
                .addDeserializer(String.class, new DoeJsonDeserializer())
                .build();

        ObjectMapper mapper = config.getObjectMapper();

        Friend friend = mapper.readValue("{\"name\":\"robert\"}", Friend.class);
        assertThat(friend.getName()).isEqualTo("Doe");
    }

    @Test
    public void canCreateStreamMapper() throws Exception {

        MappingConfig config = usingStream()
                .addDeserializer(String.class, new DoeJsonDeserializer())
                .build();

        ObjectMapper mapper = config.getObjectMapper();

        BsonByte stream = BsonByteFactory.fromDBObject(new BasicDBObject("name", "robert"));
        Friend friend = mapper.readValue(stream.getData(), Friend.class);
        assertThat(friend.getName()).isEqualTo("Doe");
    }

    private static class DoeJsonDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return "Doe";
        }
    }

    private static class DoeJsonSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString("Doe");
        }
    }
}
