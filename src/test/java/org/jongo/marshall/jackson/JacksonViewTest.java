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

import com.mongodb.DBObject;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.model.Fox;
import org.jongo.model.Views;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jongo.util.BsonUtil.bsonify;

public class JacksonViewTest {

    private JacksonEngine createProcessorWithView(final Class<?> viewClass) {
        Mapping mapping = new Mapping.Builder().withView(viewClass).build();
        return new JacksonEngine(mapping);
    }

    @Test
    public void shouldRespectJsonPublicViewOnMarshall() throws Exception {

        JacksonEngine custom = createProcessorWithView(Views.Public.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        BsonDocument doc = custom.marshall(vixen);

        DBObject result = doc.toDBObject();
        assertThat(result.get("gender")).isNull();
        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
    }

    @Test
    public void shouldRespectJsonPrivateViewOnMarshall() throws Exception {

        JacksonEngine custom = createProcessorWithView(Views.Private.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        BsonDocument doc = custom.marshall(vixen);

        DBObject result = doc.toDBObject();
        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
        assertThat(result.get("gender")).isEqualTo("female");
    }

    @Test
    public void respectsJsonPublicViewOnUnmarshall() throws Exception {

        BsonDocument doc = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        JacksonEngine custom = createProcessorWithView(Views.Public.class);

        Fox fox = custom.unmarshall(doc, Fox.class);

        assertThat(fox.getGender()).isNull();
    }

    @Test
    public void respectsJsonPrivateViewOnUnmarshall() throws Exception {

        BsonDocument doc = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        JacksonEngine custom = createProcessorWithView(Views.Private.class);

        Fox fox = custom.unmarshall(doc, Fox.class);

        assertThat(fox.getGender()).isEqualTo("female");
    }
}
