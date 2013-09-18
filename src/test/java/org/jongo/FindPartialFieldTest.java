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
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class FindPartialFieldTest extends JongoTestCase {

    private MongoCollection collection;
    private Friend friend;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
        friend = new Friend("John", "22 Wall Street Avenue");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        collection.find("{name:'John'}").projection("{name:1}").map(new AssertionResultHandler());
    }

    @Test
    public void canFindWithProjectionParams() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        collection.find("{name:'John'}").projection("{name:#}", 1).map(new AssertionResultHandler());
    }


    @Test
    public void canFindOne() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        Boolean result = collection.findOne("{name:'John'}").projection("{name:1}").map(new AssertionResultHandler());

        assertThat(result).isTrue();
    }

    @Test
    public void canFindOneWithProjectionParams() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        Boolean result = collection.findOne("{name:'John'}").projection("{name:#}", 1).map(new AssertionResultHandler());

        assertThat(result).isTrue();
    }

    @Test
    public void shouldIgnoreNullProjection() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        Friend result = collection.findOne("{name:'John'}").projection(null).as(Friend.class);

        assertThat(friend.getName()).isEqualTo("John");
        assertThat(friend.getAddress()).isEqualTo("22 Wall Street Avenue");
    }

    private static class AssertionResultHandler implements ResultHandler<Boolean> {
        public Boolean map(DBObject result) {
            assertThat(result.containsField("address")).isFalse();
            assertThat(result.containsField("name")).isTrue();
            return true;
        }
    }
}
