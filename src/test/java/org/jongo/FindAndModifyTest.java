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

import org.jongo.marshall.MarshallingException;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.jongo.util.IdResultMapper;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FindAndModifyTest extends JongoTestCase {

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
    public void canFindAndModifyOne() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend originalFriend = collection.findAndModify("{name:#}", "John").with("{$set: {address: #}}", "A better place").as(Friend.class);

        /* then */
        assertThat(originalFriend.getAddress()).isEqualTo("22 Wall Street Avenue");

        Friend updatedFriend = collection.findOne().as(Friend.class);
        assertThat(updatedFriend.getAddress()).isEqualTo("A better place");
        assertThat(updatedFriend.getName()).isEqualTo("John");
    }

    @Test
    public void canReturnNew() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend updatedFriend = collection.findAndModify().returnNew().with("{$set: {address: 'A better place'}}").as(Friend.class);

        /* then */
        assertThat(updatedFriend.getAddress()).isEqualTo("A better place");
    }

    @Test
    public void canRemove() {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend deletedFriend = collection.findAndModify().remove().as(Friend.class);

        /* then */
        assertThat(deletedFriend.getName()).isEqualTo("John");
        assertThat(collection.count()).isEqualTo(0);
    }

    @Test
    public void canSort() {
        /* given */
        collection.save(new Friend("John", "22 Wall Streem Avenue"));
        collection.save(new Friend("Wally", "22 Wall Streem Avenue"));

        /* when */
        Friend friend = collection.findAndModify()
                .sort("{name: -1}")
                .with("{$set: {address:'Sesame Street'}}")
                .as(Friend.class);

        /* then */
        assertThat(friend.getName()).isEqualTo("Wally");
    }

    @Test
    @Ignore
    public void shouldFailWhenUnableToUnmarshallResult() throws Exception {
        /* given */
        collection.insert("{error: 'NotaDate'}");

        /* when */
        try {
            collection.findAndModify("{error: 'NotaDate'}").with("{$set: {error: 'StillNotaDate'}}").as(ErrorObject.class);
            fail();
        } catch (MarshallingException e) {
            assertThat(e.getMessage()).contains(" \"error\" : \"NotaDate\"");
        }

        assertThat(collection.getDBCollection().findOne().get("error")).isEqualTo("StillNotaDate");
    }

    @Test
    public void whenNoResultShouldReturnNull() throws Exception {
        assertThat(collection.findOne("{_id:'invalid-id'}").as(Object.class)).isNull();
        assertThat(collection.findOne("{_id:'invalid-id'}").map(new IdResultMapper())).isNull();
        assertThat(collection.find("{_id:'invalid-id'}").as(Object.class)).hasSize(0);
    }
}
