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

package org.jongo.use_native;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.jongo.model.Coordinate;
import org.jongo.model.ExposableFriend;
import org.jongo.model.ExternalFriend;
import org.jongo.model.Friend;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class InsertNativeTest extends NativeTestBase {

    private MongoCollection<Friend> collection;

    @Before
    public void setUp() throws Exception {
        collection = jongo.getCollection("friends", Friend.class);
    }

    @After
    public void tearDown() throws Exception {
        collection.drop();
    }

    @Test
    public void canInsert() throws Exception {

        Friend friend = new Friend("John", "22 Wall Street Avenue");

        collection.insertOne(friend);
        long afterSave = new Date().getTime();

        Friend john = collection.find(q("{name:'John'}")).first();
        assertThat(john).isNotNull();
        assertThat(john.getId()).isNotNull();
        assertThat(john.getId().getDate().getTime()).isLessThan(afterSave);
    }

    @Test
    public void canInsertWithObjectId() throws Exception {

        ObjectId oid = ObjectId.get();
        Friend john = new Friend(oid, "John");

        collection.insertOne(john);
        long afterSave = new Date().getTime();

        Friend result = collection.find(id(oid)).first();
        assertThat(result.getId()).isEqualTo(oid);
        assertThat(john.getId().getDate().getTime()).isLessThan(afterSave);  //insert
    }

    @Test
    public void canInsertWithACustomTypeId() throws Exception {

        MongoCollection<ExternalFriend> friends = collection.withDocumentClass(ExternalFriend.class);
        ExternalFriend john = new ExternalFriend("999", "Robert");

        friends.insertOne(john);

        ExternalFriend result = friends.find().first();
        assertThat(result.getId()).isEqualTo("999");
    }

    @Test
    public void canInsertWithObjectIdAsString() throws Exception {

        MongoCollection<ExposableFriend> friends = collection.withDocumentClass(ExposableFriend.class);
        String id = ObjectId.get().toString();
        ExposableFriend john = new ExposableFriend(id, "Robert");

        friends.insertOne(john);

        ExposableFriend result = friends.find().first();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    public void canInsertAPojoWithAnEmptyObjectIdAsString() throws Exception {

        MongoCollection<ExposableFriend> friends = collection.withDocumentClass(ExposableFriend.class);
        ExposableFriend john = new ExposableFriend("Robert");

        friends.insertOne(john);

        ExposableFriend result = friends.find().first();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void canInsertAnObjectWithoutIdAnnotation() throws Exception {

        MongoCollection<Coordinate> coordinates = collection.withDocumentClass(Coordinate.class);
        Coordinate noId = new Coordinate(123, 1);

        coordinates.insertOne(noId);

        Coordinate result = coordinates.find().first();
        assertThat(result).isNotNull();
        assertThat(result.lat).isEqualTo(123);
    }

    @Test
    public void canOnlyInsertOnceAPojoWithObjectId() throws Exception {

        ObjectId id = ObjectId.get();

        collection.insertOne(new Friend(id, "John"));

        try {
            collection.insertOne(new Friend(id, "John"));
            Assert.fail();
        } catch (MongoWriteException e) {
            assertThat(e).hasMessageContaining("E11000");
        }
    }

    @Test
    public void canOnlyInsertOnceAPojoWithACustomId() throws Exception {

        MongoCollection<ExternalFriend> friends = jongo.getCollection("friends", ExternalFriend.class);

        friends.insertOne(new ExternalFriend("122", "value"));

        try {
            friends.insertOne(new ExternalFriend("122", "other value"));
            Assert.fail();
        } catch (MongoWriteException e) {
            assertThat(e).hasMessageContaining("E11000");
        }
    }

    @Test
    public void canInsertAListOfDocuments() throws Exception {

        collection.insertMany(newArrayList(new Friend("John"), new Friend("Robert")));

        assertThat(collection.countDocuments()).isEqualTo(2);
        Iterable<Friend> friends = collection.find();
        assertThat(friends).extracting("name").containsExactly("John", "Robert");
    }

}
