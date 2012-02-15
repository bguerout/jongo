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

import org.jongo.model.Poi;
import org.jongo.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.TestUtil.createEmptyCollection;
import static org.jongo.util.TestUtil.dropCollection;

public class FindSkipSortLimitTest {

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
    public void canLimit() throws Exception {
        /* given */
        collection.save(user);
        collection.save(user);
        collection.save(user);

        /* when */
        Iterator<User> results = collection.find("{}").limit(2).as(User.class);

        /* then */
        assertThat(results).hasSize(2);
    }

    @Test
    public void canSkip() throws Exception {
        /* given */
        collection.save(user);
        collection.save(user);
        collection.save(user);

        /* when */
        Iterator<User> results = collection.find("{}").skip(2).as(User.class);

        /* then */
        assertThat(results).hasSize(1);
    }

    @Test
    public void canSort() throws Exception {
        /* given */
        collection.save(new User("John", "22"));
        collection.save(new User("John", "23"));
        collection.save(new User("John", "21"));

        /* when */
        Iterator<Poi> results = collection.find("{}").sort("{'address':1}").as(Poi.class);

        /* then */
        assertThat(results.next().address).isEqualTo("21");
        assertThat(results.next().address).isEqualTo("22");
        assertThat(results.next().address).isEqualTo("23");
        assertThat(results.hasNext()).isFalse();
    }
}
