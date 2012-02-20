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

package org.jongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.TestUtil.createEmptyCollection;
import static org.jongo.util.TestUtil.dropCollection;

public class FindPartialFieldTest {

    private MongoCollection collection;
    private User user;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
        user = new User("John", "22 Wall Street Avenue");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        String id = collection.save(user);

        /* when */
        collection.find("{name:'John'}").fields("{name:1}").map(new AssertionResultMapper());
    }

    @Test
    public void canFindOne() throws Exception {
        /* given */
        String id = collection.save(user);

        /* when */
        Boolean result = collection.findOne("{name:'John'}").fields("{name:1}").map(new AssertionResultMapper());

        assertThat(result).isTrue();
    }


    private static class AssertionResultMapper implements ResultMapper<Boolean> {
        @Override
        public Boolean map(String json) {
            DBObject result = (DBObject) JSON.parse(json);
            assertThat(result.containsField("address")).isFalse();
            assertThat(result.containsField("name")).isTrue();
            return true;
        }
    }
}
