/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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
import org.jongo.MongoCursor;
import org.jongo.RawResultHandler;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.model.Gender;
import org.jongo.model.LinkedFriend;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class ParameterQueryBindingTest extends JongoTestBase {

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
    public void canBindOneParameter() throws Exception {

        long nb = collection.count("{name:#}", "robert");

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canBindManyParameter() throws Exception {

        long nb = collection.count("{name:#,address:#}", "robert", "Wall Street");

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canBindANestedField() throws Exception {

        long nb = collection.count("{coordinate.lat:#}", 2);

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canBindAPojo() throws Exception {

        long nb = collection.count("#", new Friend("robert", "Wall Street", new Coordinate(2, 3)));

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canBindEnum() throws Exception {

        Friend friend = new Friend("John", new Coordinate(2, 31));
        friend.setGender(Gender.FEMALE);
        collection.save(friend);

        Iterator<Friend> results = collection.find("{'gender':#}", Gender.FEMALE).as(Friend.class);

        assertThat(results.next().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canBindAFieldName() throws Exception {

        /* given */
        collection.insert("{name:{1:'John'}}");

        /* when */
        DBObject result = collection.findOne("{name.#:'John'}", 1).map(new RawResultHandler<DBObject>());

        /* then */
        assertThat(result).isNotNull();
        assertThat(result.get("name")).isInstanceOf(DBObject.class);
        assertThat(((DBObject) result.get("name")).get("1")).isEqualTo("John");
    }

    @Test
    // https://github.com/bguerout/jongo/issues/60
    public void canBindPattern() throws Exception {

        collection.save(new Friend("ab"));

        assertThat(collection.findOne("{name:#}", Pattern.compile("ab")).as(Friend.class)).isNotNull();
        assertThat(collection.findOne("{name:{$regex: 'ab'}}").as(Friend.class)).isNotNull();
    }

    @Test
    public void canBindTwoOidInSameQuery() throws Exception {
        /* given */
        ObjectId id1 = new ObjectId();
        Friend john = new Friend(id1, "John");
        ObjectId id2 = new ObjectId();
        Friend peter = new Friend(id2, "Peter");

        collection.save(john);
        collection.save(peter);

        MongoCursor<Friend> friends = collection.find("{$or :[{_id:{$oid:#}},{_id:{$oid:#}}]}", id1.toString(), id2.toString()).as(Friend.class);

        /* then */
        assertThat(friends.hasNext()).isTrue();
        for (Friend friend : friends) {
            assertThat(friend.getId()).isIn(id1, id2);
        }
    }

    @Test
    public void canBindOidWithASpecificName() throws Exception {
        /* given */
        ObjectId id = new ObjectId();
        LinkedFriend john = new LinkedFriend(id);
        collection.save(john);

        Iterator<LinkedFriend> friends = collection.find("{friendRelationId:{$oid:#}}", id.toString()).as(LinkedFriend.class);

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getRelationId()).isEqualTo(id);
    }

    @Test
    public void canBindNull() throws Exception {

        collection.insert("{name:null}");
        collection.insert("{name:'John'}");

        long nb = collection.count("{name:#}", null);

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canBindAHashIntoParameter() throws Exception {

        collection.insert("{name:#}", "test val#1");

        Friend friend = collection.findOne("{name:#}", "test val#1").as(Friend.class);

        assertThat(friend).isNotNull();
        assertThat(friend.getName()).isEqualTo("test val#1");
    }

    @Test
    public void canBindPrimitiveArrayParameter() throws Exception {

        collection.insert("{value:42, other:true}");

        assertThat(collection.count("{value:{$in:#}}", new int[]{42, 34})).isEqualTo(1);
        assertThat(collection.count("{value:{$in:#}}", new long[]{42})).isEqualTo(1);
        assertThat(collection.count("{value:{$in:#}}", new float[]{42})).isEqualTo(1);
        assertThat(collection.count("{other:{$in:#}}", new boolean[]{true})).isEqualTo(1);
    }


    @Test
    public void canUseListWithANullElement() throws Exception {

        collection.insert("{name:null}");
        collection.insert("{name:'John'}");

        long nb = collection.count("{name:{$in:#}}", Lists.newArrayList(1, null));

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canUseParameterWith$in() throws Exception {

        collection.insert("{value:1}");

        long nb = collection.count("{value:{$in:#}}", Lists.newArrayList(1, 2));

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void canUseParameterWith$nin() throws Exception {

        collection.insert("{value:1}");

        long nb = collection.count("{value:{$nin:#}}", Lists.newArrayList(1, 2));

        assertThat(nb).isEqualTo(1);
    }

    @Test
    // https://groups.google.com/forum/?hl=fr&fromgroups#!topic/jongo-user/ga3n5_ybYm4
    public void canUseParameterWith$push() throws Exception {

        Buddies buddies = new Buddies();
        buddies.add(new Friend("john"));
        collection.save(buddies);

        collection.update("{}").with("{$push:{friends:#}}", new Friend("peter"));

        assertThat(collection.count("{ friends.name : 'peter'}")).isEqualTo(1);
    }

    @Test
    public void canUseListParameterWith$all() throws Exception {
        collection.insert("{type:'cool', properties:['p1','p2']}");
        List<String> properties = Lists.newArrayList("p1", "p2");

        Iterator<Friend> results = collection.find("{type: #, properties: {$all: #}}", "cool", properties).as(Friend.class);

        assertThat(results.hasNext()).isTrue();
    }

    @Test
    public void canUseArrayParameterWith$all() throws Exception {
        collection.insert("{type:'cool', properties:['p1','p2']}");
        String[] properties = new String[]{"p1", "p2"};

        Iterator<Friend> results = collection.find("{type: #, properties: {$all: #}}", "cool", properties).as(Friend.class);

        assertThat(results.hasNext()).isTrue();
    }

    @Test
    public void canUseArrayParameterWith$mod() throws Exception {
        collection.insert("{value:10}");

        assertThat(collection.count("{value:{ $mod : # }}", Lists.newArrayList(10, 0))).isEqualTo(1);
    }

    @Test
    public void canUseArrayParameterWith$elemMatch() throws Exception {
        collection.insert("{x:[ { a : 1, b : 3 }, 7, { b : 99 }, { a : 11 }]}");

        assertThat(collection.count("{ x : { $elemMatch : { a : #, b : { $gt : 1 } } } }", 1)).isEqualTo(1);
    }


    @Test
    public void canUseParameterWith$exists() throws Exception {

        collection.insert("{name:'John'}");

        assertThat(collection.count("{name:{$exists:true}}")).isEqualTo(2);
    }


    @Test
    public void canUseListOfPojosParameterWith$or() throws Exception {
        collection.insert("{name:'John'}");
        collection.insert("{name:'Robert'}");
        List<Friend> friends = Lists.newArrayList(new Friend("John"), new Friend("Robert"));

        Iterator<Friend> results = collection.find("{$or : #}", friends).as(Friend.class);

        assertThat(results.hasNext()).isTrue();
    }

    @Test
    public void canUseListOfPojosParameterWith$and() throws Exception {
        collection.insert("{ name: ['John', 'Robert' ] }");
        List<Friend> friends = Lists.newArrayList(new Friend("John"), new Friend("Robert"));

        Iterator<Friend> results = collection.find("{ $and: # }", friends).as(Friend.class);

        assertThat(results.hasNext()).isTrue();
    }


    @Test
    public void shouldThrowArgumentExceptionOnInvalidQuery() throws Exception {

        try {
            collection.count("{invalid}");
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("{invalid}");
        }
    }

    @Test
    public void shouldThrowMarshallExceptionOnInvalidParameter() throws Exception {
        try {
            collection.findOne("{id:#}", new ErrorObject()).as(Friend.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getCause()).isInstanceOf(MarshallingException.class);
            assertThat(e.getMessage()).contains("{id:#}");
        }
    }

    private static class Buddies {
        private List<Friend> friends = new ArrayList<Friend>();

        public void add(Friend buddy) {
            friends.add(buddy);
        }
    }
}
