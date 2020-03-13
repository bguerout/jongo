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
import org.bson.types.ObjectId;
import org.jongo.ReflectiveObjectIdUpdater;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonIdFieldSelectorTest {

    private ReflectiveObjectIdUpdater.IdFieldSelector selector;

    @Before
    public void setUp() throws Exception {
        selector = new JacksonIdFieldSelector();
    }

    @Test
    public void withIdAnnotation() throws Exception {
        assertThat(selector.isId(IdAnnotated.class.getField("id"))).isTrue();
        assertThat(selector.isId(IdAnnotated.class.getField("id_string"))).isTrue();
        assertThat(selector.isId(IdAnnotated.class.getField("id_custom"))).isTrue();
    }

    @Test
    public void withMongoIdAnnotation() throws Exception {
        assertThat(selector.isId(MongoIdAnnotated.class.getField("id"))).isTrue();
        assertThat(selector.isId(MongoIdAnnotated.class.getField("id_string"))).isTrue();
        assertThat(selector.isId(MongoIdAnnotated.class.getField("id_custom"))).isTrue();
    }

    @Test
    public void withJsonPropertyAnnotation() throws Exception {
        assertThat(selector.isId(JsonPropertyAnnotated.class.getField("id"))).isTrue();
        assertThat(selector.isId(JsonPropertyAnnotated.class.getField("id_string"))).isTrue();
        assertThat(selector.isId(JsonPropertyAnnotated.class.getField("id_custom"))).isTrue();
        assertThat(selector.isId(JsonPropertyAnnotated.class.getField("ignored"))).isFalse();
    }

    @Test
    public void withoutAnnotation() throws Exception {
        assertThat(selector.isId(OidWithoutAnnotation.class.getField("_id"))).isTrue();
        assertThat(selector.isId(OidWithoutAnnotation.class.getField("ignored"))).isFalse();

        assertThat(selector.isId(StringWithoutAnnotation.class.getField("_id"))).isTrue();
        assertThat(selector.isId(StringWithoutAnnotation.class.getField("ignored"))).isFalse();

        assertThat(selector.isId(CustomWithoutAnnotation.class.getField("_id"))).isTrue();
    }

    @Test
    public void shouldDetectObjectIdByType() throws Exception {
        assertThat(selector.isObjectId(OidWithoutAnnotation.class.getField("_id"))).isTrue();
        assertThat(selector.isObjectId(OidWithoutAnnotation.class.getField("ignored"))).isTrue();
        assertThat(selector.isObjectId(CustomWithoutAnnotation.class.getField("_id"))).isFalse();
    }

    @Test
    public void shouldDetectObjectIdWithAnnotation() throws Exception {
        assertThat(selector.isObjectId(ObjectIdAnnotated.class.getField("_id"))).isTrue();
        assertThat(selector.isObjectId(MongoObjectIdAnnotated.class.getField("_id"))).isTrue();
        assertThat(selector.isObjectId(StringWithoutAnnotation.class.getField("_id"))).isFalse();

    }

    private static class IdAnnotated {
        @Id
        public ObjectId id;

        @Id
        public String id_string;

        @Id
        public Integer id_custom;
    }

    private static class MongoIdAnnotated {
        @MongoId
        public ObjectId id;

        @MongoId
        public String id_string;

        @MongoId
        public Integer id_custom;
    }

    private static class JsonPropertyAnnotated {
        @JsonProperty("_id")
        public ObjectId id;

        @JsonProperty("_id")
        public String id_string;


        @JsonProperty("_id")
        public Integer id_custom;

        @JsonProperty("ignored")
        public ObjectId ignored;
    }

    private static class OidWithoutAnnotation {
        public ObjectId _id;
        public ObjectId ignored;
    }

    private static class StringWithoutAnnotation {
        public String _id;
        public String ignored;
    }

    private static class CustomWithoutAnnotation {
        public Integer _id;
    }

    private static class ObjectIdAnnotated {
        @org.jongo.marshall.jackson.oid.ObjectId
        public String _id;
    }

    private static class MongoObjectIdAnnotated {
        @MongoObjectId
        public String _id;
    }

}
