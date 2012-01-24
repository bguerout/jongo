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

package com.jongo.jackson;

import com.jongo.model.Poi;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class JacksonProcessorTest {

    private JacksonProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new JacksonProcessor();
    }

    @Test
    public void canConvertJsonToEntity() throws IOException {

        DBObject dbObject = new BasicDBObject("address", "22 rue des murlins");

        Poi poi = processor.unmarshall(dbObject.toString(), Poi.class);

        assertThat(poi.address).isEqualTo("22 rue des murlins");
    }

    @Test
    public void canConvertNestedJsonToEntities() throws IOException {

        DBObject dbObject = new BasicDBObject("address", "22 rue des murlins");
        dbObject.put("coordinate", new BasicDBObject("lat", "48"));

        Poi poi = processor.unmarshall(dbObject.toString(), Poi.class);

        assertThat(poi.coordinate.lat).isEqualTo(48);

    }
}
