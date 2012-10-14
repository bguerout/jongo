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

import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class FindWithParametersTest extends JongoTestCase {

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
    public void canFindOne() throws Exception {
        /* given */
        collection.insert("{name:'John'}");

        /* when */
        Friend result = collection.findOne("{name:#}", "John").as(Friend.class);

        /* then */
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        collection.insert("{name:'John'}");

        /* when */
        Iterator<Friend> friends = collection.find("{name:#}", "John").as(Friend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getName()).isEqualTo("John");
        assertThat(friends.hasNext()).isFalse();
    }

}

