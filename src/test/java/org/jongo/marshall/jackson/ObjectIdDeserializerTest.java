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
import junit.framework.Assert;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ObjectIdDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ObjectIdDeserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        mapper.registerModule(module);
    }

    @Test
    public void shouldDeserializeId() throws Exception {

        Identifier identifier = mapper.readValue("{ \"_id\" : \"4f92d1ae44ae2dac4527d49b\"}", Identifier.class);

        assertThat(identifier._id).isEqualTo("4f92d1ae44ae2dac4527d49b");
    }

    @Test
    public void shouldDeserialize$oid() throws Exception {

        Identifier identifier = mapper.readValue("{ \"_id\" : { \"$oid\" : \"4f92d1ae44ae2dac4527d49b\"}}", Identifier.class);

        assertThat(identifier._id).isEqualTo("4f92d1ae44ae2dac4527d49b");
    }

    @Test
    public void shouldFailOnInvalidId() throws Exception {

        try {
            mapper.readValue("{ \"_id\" : { \"$invalid\" : \"wrong\"}}", Identifier.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(IllegalArgumentException.class);
        }

    }

    private static class Identifier {
        public ObjectId _id;
    }
}
