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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.DBObject;
import org.jongo.model.Fox;
import org.jongo.model.Views;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.BSON.bsonify;

public class JacksonViewTest {


    @Test
    @Ignore
    public void shouldRespectJsonPublicViewOnMarshall() throws Exception {

        BsonProcessor custom = createProcessorWithView(Views.Public.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        DBObject result = custom.marshall(vixen);

        assertThat(result.get("gender")).isNull();
        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
    }

    @Test
    @Ignore
    public void shouldRespectJsonPrivateViewOnMarshall() throws Exception {

        BsonProcessor custom = createProcessorWithView(Views.Private.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        DBObject result = custom.marshall(vixen);

        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
        assertThat(result.get("gender")).isEqualTo("female");
    }

    @Test
    @Ignore
    public void respectsJsonPublicViewOnUnmarshall() throws Exception {

        DBObject json = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        BsonProcessor custom = createProcessorWithView(Views.Public.class);

        Fox fox = custom.unmarshall(json, Fox.class);

        assertThat(fox.getGender()).isNull();
    }

    @Test
    @Ignore
    public void respectsJsonPrivateViewOnUnmarshall() throws Exception {

        DBObject json = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        BsonProcessor custom = createProcessorWithView(Views.Private.class);

        Fox fox = custom.unmarshall(json, Fox.class);

        assertThat(fox.getGender()).isEqualTo("female");
    }


    private BsonProcessor createProcessorWithView(Class<?> viewClass) {
        ObjectMapper mapper = new ObjectMapperFactory().createBsonMapper();
        ObjectReader reader = mapper.readerWithView(viewClass);
        ObjectWriter writer = mapper.writerWithView(viewClass);
        return new BsonProcessor(mapper);
    }
}
