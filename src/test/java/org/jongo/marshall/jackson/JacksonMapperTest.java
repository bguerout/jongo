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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.jongo.Mapper;
import org.jongo.ObjectIdUpdater;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.model.ExposableFriend;
import org.jongo.model.Friend;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class JacksonMapperTest {

    @Test
    public void canAddDeserializer() throws Exception {

        BsonDocument document = Bson.createDocument(new BasicDBObject("name", "robert"));
        Mapper mapper = jacksonMapper()
                .addDeserializer(String.class, new DoeJsonDeserializer())
                .build();

        Friend friend = mapper.getUnmarshaller().unmarshall(document, Friend.class);

        assertThat(friend.getName()).isEqualTo("Doe");
    }

    @Test
    public void canAddSerializer() throws Exception {

        Friend robert = new Friend("Robert");
        Mapper mapper = jacksonMapper()
                .addSerializer(String.class, new DoeJsonSerializer())
                .build();

        BsonDocument document = mapper.getMarshaller().marshall(robert);

        assertThat(document.toString()).contains("{\"name\": \"Doe\"}");
    }

    @Test
    public void canSetVisibilityChecker() throws Exception {

        PojoWithGetter robert = new PojoWithGetter("Robert", "Sax");

        Mapper mapper = jacksonMapper()
                .setVisibilityChecker(new VisibilityChecker.Std(JsonAutoDetect.Visibility.PUBLIC_ONLY).withFieldVisibility(JsonAutoDetect.Visibility.NONE))
                .build();

        BsonDocument document = mapper.getMarshaller().marshall(robert);

        assertThat(document.toString()).isEqualTo("{\"firstName\": \"Robert\"}");
    }

    @SuppressWarnings("serial")
    @Test
    public void canUseAnnotations() throws Exception {
        String id = "563667f82249254c42530fe3";
        ExposableFriend external = new ExposableFriend(id, "Robert");

        Mapper mapper = jacksonMapper().build();

        BsonDocument document = mapper.getMarshaller().marshall(external);

        assertThat(document.toString()).isEqualTo("{\"_id\": {\"$oid\": \"563667f82249254c42530fe3\"}, \"name\": \"Robert\"}");
    }

    @Test
    public void canAddModule() throws Exception {

        ObjectId oid = new ObjectId("504482e5e4b0d1b2c47fff66");
        Friend friend = new Friend(oid, "Robert");
        Mapper mapper = jacksonMapper()
                .registerModule(new Module() {
                    @Override
                    public String getModuleName() {
                        return "test-module";
                    }

                    @Override
                    public Version version() {
                        return new Version(2, 0, 0, "", "org.jongo", "testmodule");
                    }

                    @Override
                    public void setupModule(SetupContext setupContext) {
                        SimpleDeserializers deserializers = new SimpleDeserializers();
                        deserializers.addDeserializer(String.class, new DoeJsonDeserializer());
                        setupContext.addDeserializers(deserializers);
                    }
                })
                .build();

        BsonDocument document = mapper.getMarshaller().marshall(friend);

        assertThat(document.toString()).contains("\"_id\": {\"$oid\": \"504482e5e4b0d1b2c47fff66\"}");
    }

    @Test
    public void canAddJongoInterfaces() throws Exception {

        ObjectIdUpdater objectIdUpdater = new ObjectIdUpdater() {
            public boolean mustGenerateObjectId(Object pojo) {
                return false;
            }

            public void setObjectId(Object newPojo, ObjectId id) {
            }

            public Object getId(Object pojo) {
                return null;
            }
        };

        QueryFactory factory = new QueryFactory() {
            public Query createQuery(String query, Object... parameters) {
                return null;
            }
        };
        Mapper mapper = jacksonMapper()
                .withObjectIdUpdater(objectIdUpdater)
                .withQueryFactory(factory)
                .build();

        assertThat(mapper.getObjectIdUpdater()).isEqualTo(objectIdUpdater);
        assertThat(mapper.getQueryFactory()).isEqualTo(factory);
    }

    private static class DoeJsonSerializer extends JsonSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString("Doe");
        }
    }

    private static class DoeJsonDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return "Doe";
        }
    }

    private static class PojoWithGetter {
        private String firstName;
        private String lastName;

        private PojoWithGetter(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }
    }
}
