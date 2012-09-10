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

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.mongodb.DBObject;
import org.jongo.marshall.jackson.configuration.DeserializationBehavior;
import org.jongo.marshall.jackson.configuration.JacksonConfiguration;
import org.jongo.marshall.jackson.configuration.SerializationBehavior;
import org.jongo.model.Fox;
import org.jongo.model.Views;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.BSON.bsonify;

public class JacksonViewTest {


    @Test
    public void shouldRespectJsonPublicViewOnMarshall() throws Exception {

        JacksonProcessor custom = createProcessorWithView(Views.Public.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        DBObject result = custom.marshall(vixen);

        assertThat(result.get("gender")).isNull();
        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
    }

    @Test
    public void shouldRespectJsonPrivateViewOnMarshall() throws Exception {

        JacksonProcessor custom = createProcessorWithView(Views.Private.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        DBObject result = custom.marshall(vixen);

        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
        assertThat(result.get("gender")).isEqualTo("female");
    }

    @Test
    public void respectsJsonPublicViewOnUnmarshall() throws Exception {

        DBObject json = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        JacksonProcessor custom = createProcessorWithView(Views.Public.class);

        Fox fox = custom.unmarshall(json, Fox.class);

        assertThat(fox.getGender()).isNull();
    }

    @Test
    public void respectsJsonPrivateViewOnUnmarshall() throws Exception {

        DBObject json = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        JacksonProcessor custom = createProcessorWithView(Views.Private.class);

        Fox fox = custom.unmarshall(json, Fox.class);

        assertThat(fox.getGender()).isEqualTo("female");
    }

    private JacksonProcessor createProcessorWithView(Class<?> viewClass) {
        ObjectMapper objectMapper = new JacksonConfiguration()
                .addBehaviour(new SerializationBehavior())
                .addBehaviour(new DeserializationBehavior())
                .addModule(new JsonModule())
                .configureMapper(new ViewObjectMapper(viewClass));
        return new JacksonProcessor(objectMapper);
    }

    private static class ViewObjectMapper extends ObjectMapper {
        private final Class<?> viewClass;

        public ViewObjectMapper(Class<?> viewClass) {
            this.viewClass = viewClass;
        }

        @Override
        public DeserializationConfig getDeserializationConfig() {
            return super.getDeserializationConfig().withView(viewClass);
        }

        @Override
        public SerializationConfig getSerializationConfig() {
            return super.getSerializationConfig().withView(viewClass);
        }
    }
}
