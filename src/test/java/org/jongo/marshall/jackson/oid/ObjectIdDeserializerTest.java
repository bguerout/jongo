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

package org.jongo.marshall.jackson.oid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;


/**
 * Test for {@link org.jongo.marshall.jackson.oid.ObjectIdDeserializer}
 *
 * @author sukrit007
 */
public class ObjectIdDeserializerTest {

    /**
     * Test class for testing _id de-serialization
     */
    static class TestClass {
        @ObjectId
        String _id;
    }

    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void shouldDeSerializeIdString() throws IOException {
        TestClass testObj = objectMapper.readValue("{\"_id\":\"53a499be60b2a2248d956875\"}", TestClass.class);
        Assert.assertThat("The id gets de-serialized to expected value", testObj._id,
                equalTo("53a499be60b2a2248d956875"));
    }
}
