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

package org.jongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NonPojoTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canSaveMapWithObjectId() throws Exception {
        ObjectId id = ObjectId.get();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "John");
        map.put("_id", id);

        collection.save(map);

        Map result = collection.findOne().as(Map.class);
        assertThat(result.get("_id")).isEqualTo(id);
    }

    @Test
    public void canSaveANewMap() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "John");

        collection.save(map);

        Map result = collection.findOne().as(Map.class);
        assertThat(result.get("_id")).isNotNull();
    }

    @Test
    public void canFindAndSaveMap() throws Exception {

        collection.insert("{name:'John'}");
        Map<String, String> map = collection.findOne().as(Map.class);
        map.put("name", "Robert");

        collection.save(map);

        Map result = collection.findOne().as(Map.class);
        assertThat(result.get("name")).isEqualTo("Robert");
        assertThat(result.get("_id")).isNotNull();
    }

    @Test
    public void canSaveANewJsonNode() throws Exception {
        JsonNodeFactory factory = new JsonNodeFactory(false);
        ObjectNode node = factory.objectNode();
        node.put("test", "value");

        collection.save(node);

        JsonNode result = collection.findOne().as(JsonNode.class);
        assertThat(result.get("_id")).isNotNull();
        assertThat(result.get("test").asText()).isEqualTo("value");
    }

    @Test
    public void canFindAndSaveJsonNode() throws Exception {

        collection.insert("{name:'John'}");
        ObjectNode node = collection.findOne().as(ObjectNode.class);
        node.put("name", "Robert");

        collection.save(node);

        Map result = collection.findOne().as(Map.class);
        assertThat(result.get("name")).isEqualTo("Robert");
        assertThat(result.get("_id")).isNotNull();
    }
}