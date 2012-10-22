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

package org.jongo.marshall;

import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.model.Gender;
import org.jongo.model.LinkedFriend;
import org.jongo.util.DBObjectResultMapper;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

public class ParameterQueryBindingTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("marshalling");
        collection.save(new Friend("robert", "Wall Street", new Coordinate(2, 3)));
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("marshalling");
    }

    @Test
    public void shouldBindOneParameter() throws Exception {

        long nb = collection.count("{name:#}", "robert");

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindManyParameter() throws Exception {

        long nb = collection.count("{name:#,address:#}", "robert", "Wall Street");

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindListOfPrimitive() throws Exception {

        collection.insert("{index:1}");

        List<Integer> indexes = Lists.newArrayList(1, 2);

        long nb = collection.count("{index:{$in:#}}", indexes);

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindParametersOnNestedFields() throws Exception {

        long nb = collection.count("{coordinate.lat:#}", 2);

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindAPojo() throws Exception {

        long nb = collection.count("#", new Friend("robert", "Wall Street", new Coordinate(2, 3)));

        assertThat(nb).isEqualTo(1);
    }

    @Test
    // https://groups.google.com/forum/?hl=fr&fromgroups#!topic/jongo-user/ga3n5_ybYm4
    public void shouldBindAListOfPojo() throws Exception {

        Buddies buddies = new Buddies();
        buddies.add(new Friend("john"));
        collection.save(buddies);

        collection.update("{}").with("{$push:{friends:#}}", new Friend("peter"));

        assertThat(collection.count("{ friends.name : 'peter'}")).isEqualTo(1);
    }

    @Test
    public void shouldBindAFieldName() throws Exception {

        /* given */
        collection.insert("{name:{1:'John'}}");

        /* when */
        DBObject result = collection.findOne("{name.#:#}", 1, "John").map(new DBObjectResultMapper());

        /* then */
        assertThat(result).isNotNull();
        assertThat(result.get("name")).isInstanceOf(DBObject.class);
        assertThat(((DBObject) result.get("name")).get("1")).isEqualTo("John");
    }

    @Test
    public void shouldBindEnumParameter() throws Exception {

        Friend friend = new Friend("John", new Coordinate(2, 31));
        friend.setGender(Gender.FEMALE);
        collection.save(friend);

        Iterator<Friend> results = collection.find("{'gender':#}", Gender.FEMALE).as(Friend.class).iterator();

        assertThat(results.next().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    // https://github.com/bguerout/jongo/issues/60
    public void shouldBindPatterns() throws Exception {

        collection.save(new Friend("ab"));

        assertThat(collection.findOne("{name:{$regex: 'ab'}}").as(Friend.class)).isNotNull();
        assertThat(collection.findOne("{name:#}", Pattern.compile("ab")).as(Friend.class)).isNotNull();
    }

    @Test
    public void shouldThrowExceptionOnInvalidQuery() throws Exception {

        try {
            collection.count("{invalid}");
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("{invalid}");
        }
    }

    @Test
    public void shouldFailWithInvalidParameter() throws Exception {
        try {
            collection.findOne("{id:#}", new ErrorObject()).as(Friend.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("{id:#}");
        }
    }

    @Test
    public void canFindWithTwoOid() throws Exception {
        /* given */
        ObjectId id1 = new ObjectId();
        Friend john = new Friend(id1, "John");
        ObjectId id2 = new ObjectId();
        Friend peter = new Friend(id2, "Peter");

        collection.save(john);
        collection.save(peter);

        Iterable<Friend> friends = collection.find("{$or :[{_id:{$oid:#}},{_id:{$oid:#}}]}", id1.toString(), id2.toString()).as(Friend.class);

        /* then */
        assertThat(friends.iterator().hasNext()).isTrue();
        for (Friend friend : friends) {
            assertThat(friend.getId()).isIn(id1, id2);
        }
    }

    @Test
    public void canFindWithOidNamed() throws Exception {
        /* given */
        ObjectId id = new ObjectId();
        LinkedFriend john = new LinkedFriend(id);
        collection.save(john);

        Iterator<LinkedFriend> friends = collection.find("{friendRelationId:{$oid:#}}", id.toString()).as(LinkedFriend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getRelationId()).isEqualTo(id);
    }

    private static class Buddies {
        private List<Friend> friends = new ArrayList<Friend>();

        public void add(Friend buddy) {
            friends.add(buddy);
        }
    }

}
