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

package org.jongo.spike.dbref;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.jongo.spike.dbref.jackson.Reference;
import org.jongo.spike.dbref.jackson.ReferenceLink;
import org.jongo.spike.dbref.jackson.ReferenceSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

public class ReferenceSerializerTest {

    private ObjectMapper mapper;
    private ReferenceSerializer serializer;
    private Buddy buddyWithAFriend;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        serializer = new ReferenceSerializer();
        buddyWithAFriend = new Buddy("john", new Buddy("pal", null));

        SimpleModule module = new SimpleModule("module", new Version(1, 0, 0, null));
        module.addSerializer(Reference.class, serializer);
        mapper.registerModule(module);
    }

    @Test
    public void shouldFailWhenNoLinksRegistered() throws Exception {
        try {
            mapper.writeValueAsString(buddyWithAFriend);
            fail("Should have thrown an exception");
        } catch (IOException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void shouldFailWhenObjectHasANullId() throws Exception {
        serializer.registerReferenceLink(Buddy.class, new FakeReferenceLink(withNullId()));

        try {
            mapper.writeValueAsString(buddyWithAFriend);
            fail("Should have thrown an exception");
        } catch (IOException e) {
            assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
        }

    }

    @Test
    public void shouldSerializeDBRef() throws Exception {

        serializer.registerReferenceLink(Buddy.class, new FakeReferenceLink("idOfAFriend"));

        String asString = mapper.writeValueAsString(buddyWithAFriend);

        assertThat(asString).contains("{\"friend\":{ \"$ref\" : \"buddies\", \"$id\" : \"idOfAFriend\" }");


    }

    private String withNullId() {
        return null;
    }


    private static class FakeReferenceLink implements ReferenceLink<Buddy> {

        private String id;

        private FakeReferenceLink(String id) {
            this.id = id;
        }

        public String getReferenceCollectionName(Buddy buddy) {
            return "buddies";
        }

        public String getId(Buddy buddy) {
            return id;
        }
    }
}
