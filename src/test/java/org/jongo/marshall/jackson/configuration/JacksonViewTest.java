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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.DBObject;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.model.Fox;
import org.jongo.model.Views;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.BSON.bsonify;

public class JacksonViewTest {


    private JacksonProcessor createProcessorWithView(final Class<?> viewClass) {
        MappingConfig conf = MappingConfigBuilder.usingJson()
                .setReaderCallback(new ViewReaderCallback(viewClass))
                .setWriterCallback(new ViewWriterCallback(viewClass))
                .createConfiguration();
        return new JacksonProcessor(conf);
    }

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

    private static class ViewWriterCallback implements WriterCallback {
        private final Class<?> viewClass;

        public ViewWriterCallback(Class<?> viewClass) {
            this.viewClass = viewClass;
        }

        public ObjectWriter getWriter(ObjectMapper mapper, Object pojo) {
            return mapper.writerWithView(viewClass);
        }
    }

    private static class ViewReaderCallback implements ReaderCallback {
        private final Class<?> viewClass;

        public ViewReaderCallback(Class<?> viewClass) {
            this.viewClass = viewClass;
        }

        public ObjectReader getReader(ObjectMapper mapper, Class<?> clazz) {
            return mapper.reader(clazz).withView(viewClass);
        }
    }
}
