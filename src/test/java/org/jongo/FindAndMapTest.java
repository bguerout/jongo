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

import org.jongo.util.IdResultMapper;
import org.jongo.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.TestUtil.createEmptyCollection;
import static org.jongo.util.TestUtil.dropCollection;

public class FindAndMapTest {

    private MongoCollection collection;
    private User user;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("jongo", "users");
        user = new User("John", "22 Wall Street Avenue");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("jongo", "users");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        String id = collection.save(user);
        String id2 = collection.save(new User("Smith", "23 Wall Street Avenue"));
        String id3 = collection.save(new User("Peter", "24 Wall Street Avenue"));

        /* when */
        Iterator<String> userIds = collection.find("{}").map(new IdResultMapper());

        /* then */
        assertThat(userIds.next()).isEqualTo(id);
        assertThat(userIds.next()).isEqualTo(id2);
        assertThat(userIds.next()).isEqualTo(id3);
        assertThat(userIds.hasNext()).isFalse();
    }

    @Test
    public void canFindOne() throws Exception {
        /* given */
        String id = collection.save(user);

        /* when */
        String userId = collection.findOne("{}").map(new IdResultMapper());

        /* then */
        assertThat(userId).isEqualTo(id);

    }
}
