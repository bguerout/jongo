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

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.jongo.marshall.MarshallingException;
import org.jongo.model.ExposableFriend;
import org.jongo.model.ExternalFriend;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateNativeTest extends NativeTestBase {

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
    public void canUpdateAnEntity() throws Exception {

        Friend john = new Friend("John", "21 Jump Street");
        collection.insertOne(john);

        john.setAddress("new address");
        collection.replaceOne(id(john.getId()), john);

        ObjectId johnId = john.getId();
        Friend johnWithNewAddress = collection.find(id(johnId)).first();
        assertThat(johnWithNewAddress.getId()).isEqualTo(johnId);
        assertThat(johnWithNewAddress.getAddress()).isEqualTo("new address");
    }

    @Test
    public void canUpdateWithACustomTypeId() throws Exception {

        MongoCollection<ExternalFriend> friends = collection.withDocumentClass(ExternalFriend.class);
        ExternalFriend friend = new ExternalFriend("999", "Robert");
        friends.insertOne(friend);

        friend.setName("Robert");
        friends.replaceOne(id(friend.getId()), friend);

        ExternalFriend result = friends.find().first();
        assertThat(result.getId()).isEqualTo("999");
    }

    @Test
    public void canUpdateWithObjectIdAsString() throws Exception {

        MongoCollection<ExposableFriend> friends = collection.withDocumentClass(ExposableFriend.class);
        String id = ObjectId.get().toString();
        ExposableFriend robert = new ExposableFriend(id, "Robert");
        friends.insertOne(robert);

        robert.setName("Hue"); // <-- "famous" french Robert
        friends.replaceOne(q("{_id:{$oid:#}}", id), robert);

        ExposableFriend robertHue = friends.find(q("{_id:{$oid:#}}", id)).first();
        assertThat(robertHue.getId()).isEqualTo(id);
        assertThat(robertHue.getName()).isEqualTo("Hue");
    }

    @Test
    public void canUpdateAPojoWithACustomId() throws Exception {

        MongoCollection<ExternalFriend> friends = collection.withDocumentClass(ExternalFriend.class);
        ExternalFriend externalFriend = new ExternalFriend("122", "John");
        MongoCollection<ExternalFriend> safeCollection = friends.withWriteConcern(WriteConcern.ACKNOWLEDGED);

        safeCollection.insertOne(externalFriend);
        externalFriend.setName("Robert");
        safeCollection.replaceOne(id(externalFriend.getId()), externalFriend);

        ExternalFriend result = friends.find(q("{name:'Robert'}")).first();
        assertThat(result.getId()).isEqualTo("122");
    }

    @Test
    public void canUpdateAPojoWithAnValidObjectIdAsString() {

        MongoCollection<ExposableFriend> friends = collection.withDocumentClass(ExposableFriend.class);
        ExposableFriend friend = new ExposableFriend(ObjectId.get().toString(), "Robert");

        friends.insertOne(friend);
        String id = friend.getId();
        assertThat(friend.getId()).isNotNull();

        friend.setName("John");
        friends.replaceOne(id(friend.getId()), friend);

        assertThat(friend.getId()).isEqualTo(id);
        assertThat(friend.getName()).isEqualTo("John");
    }

    @Test
    public void shouldFailWhenMarshallerFail() throws Exception {

        try {
            collection.withDocumentClass(ErrorObject.class).insertOne(new ErrorObject());
            Assert.fail();
        } catch (MarshallingException e) {
        }
    }

}
