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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jongo.Mapper;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class JacksonMixinTest {

    @SuppressWarnings("serial")
    @Test
    public void canUseMixins() throws Exception {
        String id = "563667f82249254c42530fe3";
        ExternalType external = new ExternalType(id, "Robert");

        Mapper mapper = jacksonMapper()
                .registerModule(new SimpleModule() {{
                    this.setMixInAnnotation(ExternalType.class, ExternalType.ExternalTypeMixin.class);
                }})
                .build();

        BsonDocument document = mapper.getMarshaller().marshall(external);

        assertThat(document.toString()).isEqualTo("{ \"_id\" : { \"$oid\" : \"" + id + "\"} , \"name\" : \"Robert\"}");
    }

    /**
     * Models a type coming from a third party tool like JsonSchema2Pojo.
     *
     * @author Christian Trimble
     */
    @SuppressWarnings("deprecation")
    public static class ExternalType {

        /**
         * Mixin that supplies all of the mongo specific annotations.
         *
         * @author Christian Trimble
         */
        public static abstract class ExternalTypeMixin {
            @MongoObjectId
            @MongoId
            @org.jongo.marshall.jackson.oid.ObjectId
            @Id
            public String id;

            @MongoObjectId
            @MongoId
            @org.jongo.marshall.jackson.oid.ObjectId
            @Id
            public abstract String getId();

            @MongoObjectId
            @MongoId
            @org.jongo.marshall.jackson.oid.ObjectId
            @Id
            public abstract void setId(String id);
        }

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        public ExternalType() {
        }

        public ExternalType(String name) {
            this.name = name;
        }

        public ExternalType(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @JsonProperty("id")
        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        @JsonProperty("name")
        public void setName(String name) {
            this.name = name;
        }
    }


}
