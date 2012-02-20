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

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.jongo.model.Fox;
import org.jongo.model.Poi;
import org.jongo.model.User;
import org.junit.Before;
import org.junit.Test;

public class JacksonProcessorTest {

    private JacksonProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new JacksonProcessor();
    }

    @Test
    public void canConvertEntityToJson() {
        String json = processor.marshall(new Fox("fantastic", "roux"));
        assertThat(json).isEqualTo(jsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux'}"));

        User user = processor.unmarshall(json, User.class);
        assertThat(user.getName()).isEqualTo("fantastic");
    }

    @Test
    public void canConvertJsonToEntity() throws IOException {
        String json = jsonify("{'address': '22 rue des murlins'}");

        Poi poi = processor.unmarshall(json, Poi.class);

        assertThat(poi.address).isEqualTo("22 rue des murlins");
        assertThat(poi.address).isEqualTo("22 rue des murlins");
    }

    @Test
    public void canConvertNestedJsonToEntities() throws IOException {
        String json = jsonify("{'address': '22 rue des murlins', 'coordinate': {'lat': 48}}");

        Poi poi = processor.unmarshall(json, Poi.class);

        assertThat(poi.coordinate.lat).isEqualTo(48);
    }

    private String jsonify(String json) {
        return json.replace("'", "\"");
    }
}
