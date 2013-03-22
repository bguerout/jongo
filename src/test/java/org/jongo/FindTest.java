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

import com.mongodb.ReadPreference;
import org.bson.types.ObjectId;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.Oid.withOid;

public class FindTest extends JongoTestCase {

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
    public void canFind() throws Exception {
        /* given */
        Friend friend = new Friend(new ObjectId(), "John");
        collection.save(friend);

        /* when */
        Iterator<Friend> friends = collection.find("{name:'John'}").as(Friend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getName()).isEqualTo("John");
        assertThat(friends.hasNext()).isFalse();
    }

    @Test
    public void shouldFailWhenUnableToUnmarshallResult() throws Exception {
        /* given */
        collection.insert("{error: 'NotaDate'}");

        /* when */
        Iterator<ErrorObject> results = collection.find().as(ErrorObject.class).iterator();

        try {
            results.next();
            fail();
        } catch (MarshallingException e) {
            assertThat(e.getMessage()).contains(" \"error\" : \"NotaDate\"");
        }
    }

    @Test
    public void canFindWithEmptySelector() throws Exception {
        /* given */
        collection.insert("{name:'John'}");
        collection.insert("{name:'Smith'}");
        collection.insert("{name:'Peter'}");

        /* when */
        Iterable<Friend> friends = collection.find().as(Friend.class);

        /* then */
        assertThat(friends.iterator().hasNext()).isTrue();
        for (Friend friend : friends) {
            assertThat(friend.getName()).isIn("John", "Smith", "Peter");
        }
    }

    @Test
    public void canFindUsingSubProperty() throws Exception {
        /* given */
        collection.save(new Friend("John", new Coordinate(2, 31)));

        /* when */
        Iterator<Friend> results = collection.find("{'coordinate.lat':2}").as(Friend.class).iterator();

        /* then */
        assertThat(results.next().getCoordinate().lat).isEqualTo(2);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canFindWithOid() throws Exception {
        /* given */
        ObjectId id = new ObjectId();
        Friend john = new Friend(id, "John");
        collection.save(john);

        Iterator<Friend> friends = collection.find("{_id:{$oid:#}}", id.toString()).as(Friend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getId()).isEqualTo(id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullObjectId() throws Exception {
        collection.findOne((ObjectId) null);
    }

    @Test
    public void canFindWithStringAsObjectId() {
        /* given */
        ObjectId id = new ObjectId();
        ExposableFriend friend = new ExposableFriend(id, "John");
        collection.save(friend);

        /* when */
        Iterator<Friend> friends = collection.find(withOid(friend.getId())).as(Friend.class).iterator();

        /* then */
        Friend john = friends.next();
        assertThat(john.getId()).isEqualTo(id);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(friends.hasNext()).isFalse();
    }

    @Test
    public void canFindWithReadPreference() throws Exception {
        /* given */
        Friend friend = new Friend(new ObjectId(), "John");
        collection.save(friend);

        /* when */
        Iterator<Friend> friends = collection.withReadPreference(ReadPreference.primaryPreferred()).find("{name:'John'}").as(Friend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getName()).isEqualTo("John");
        assertThat(friends.hasNext()).isFalse();

        // warning: we cannot check that ReadPreference is really used by driver, this unit test only checks the API
    }

    private static class ExposableFriend {

        @Id
        @org.jongo.marshall.jackson.oid.ObjectId
        private String id;
        private String name;

        public ExposableFriend(ObjectId id, String name) {
            this.id = id.toString();
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
