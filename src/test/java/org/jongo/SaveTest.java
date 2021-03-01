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

package org.jongo;

import com.mongodb.WriteConcern;
import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.jongo.model.*;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class SaveTest extends JongoTestBase {

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
    public void canSave() throws Exception {

        Friend friend = new Friend("John", "22 Wall Street Avenue");

        collection.save(friend);
        long afterSave = new Date().getTime();

        Friend john = collection.findOne("{name:'John'}").as(Friend.class);
        assertThat(john).isNotNull();
        assertThat(john.getId()).isNotNull();
        assertThat(john.getId().getDate().getTime()).isLessThan(afterSave);
    }

    @Test
    public void canSaveWithObjectId() throws Exception {

        ObjectId oid = ObjectId.get();
        Friend john = new Friend(oid, "John");

        collection.save(john);
        long afterSave = new Date().getTime();

        Friend result = collection.findOne(oid).as(Friend.class);
        assertThat(result.getId()).isEqualTo(oid);
        assertThat(john.getId().getDate().getTime()).isLessThan(afterSave);  //insert
    }

    @Test
    public void canUpdateAnEntity() throws Exception {

        Friend john = new Friend("John", "21 Jump Street");
        collection.save(john);

        john.setAddress("new address");
        collection.save(john);

        ObjectId johnId = john.getId();
        Friend johnWithNewAddress = collection.findOne(johnId).as(Friend.class);
        assertThat(johnWithNewAddress.getId()).isEqualTo(johnId);
        assertThat(johnWithNewAddress.getAddress()).isEqualTo("new address");
    }

    @Test
    public void canSaveWithACustomTypeId() throws Exception {

        ExternalFriend john = new ExternalFriend("999", "Robert");

        collection.save(john);

        ExternalFriend result = collection.findOne().as(ExternalFriend.class);
        assertThat(result.getId()).isEqualTo("999");
    }

    @Test
    public void canUpdateWithACustomTypeId() throws Exception {

        ExternalFriend friend = new ExternalFriend("999", "Robert");
        collection.save(friend);

        friend.setName("Robert");
        collection.save(friend);

        ExternalFriend result = collection.findOne().as(ExternalFriend.class);
        assertThat(result.getId()).isEqualTo("999");
    }

    @Test
    public void canSaveWithObjectIdAsString() throws Exception {

        String id = ObjectId.get().toString();
        ExposableFriend john = new ExposableFriend(id, "Robert");

        collection.save(john);

        ExposableFriend result = collection.findOne().as(ExposableFriend.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    public void canUpdateWithObjectIdAsString() throws Exception {
        String id = ObjectId.get().toString();
        ExposableFriend robert = new ExposableFriend(id, "Robert");

        collection.save(robert);
        String johnId = robert.getId();

        robert.setName("Hue"); // <-- "famous" french Robert
        collection.save(robert);

        ExposableFriend robertHue = collection.findOne("{_id:{$oid:#}}", johnId).as(ExposableFriend.class);
        assertThat(robertHue.getId()).isEqualTo(johnId);
        assertThat(robertHue.getName()).isEqualTo("Hue");
    }

    @Test
    public void canUpdateWithObjectId() throws Exception {
        String id = ObjectId.get().toString();
        ExposableFriend robert = new ExposableFriend(id, "Robert");

        collection.save(robert);
        assertThat(robert.getId()).isEqualTo(id);

        robert.setName("Hue");
        collection.save(robert);

        ExposableFriend robertHue = collection.findOne("{_id: #}", new ObjectId(id)).as(ExposableFriend.class);
        assertThat(robertHue.getId()).isEqualTo(id);
        assertThat(robertHue.getName()).isEqualTo("Hue");
    }

    @Test
    public void canUpdateAPojoWithACustomId() throws Exception {

        ExternalFriend externalFriend = new ExternalFriend("122", "John");
        MongoCollection safeCollection = collection.withWriteConcern(WriteConcern.MAJORITY);

        safeCollection.save(externalFriend);
        externalFriend.setName("Robert");
        safeCollection.save(externalFriend);

        ExternalFriend result = collection.findOne("{name:'Robert'}").as(ExternalFriend.class);
        assertThat(result.getId()).isEqualTo("122");
    }

    @Test
    public void canSaveAPojoWithAnEmptyObjectIdAsString() throws Exception {

        ExposableFriend john = new ExposableFriend("Robert");

        collection.save(john);

        ExposableFriend result = collection.findOne().as(ExposableFriend.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void canUpdateAPojoWithAnValidObjectIdAsString() {

        ExposableFriend friend = new ExposableFriend(ObjectId.get().toString(), "Robert");

        collection.save(friend);
        String id = friend.getId();
        assertThat(friend.getId()).isNotNull();

        friend.setName("John");
        collection.save(friend);

        assertThat(friend.getId()).isEqualTo(id);
        assertThat(friend.getName()).isEqualTo("John");
    }

    @Test
    public void canSaveAnObjectWithoutIdAnnotation() throws Exception {

        Coordinate noId = new Coordinate(123, 1);

        collection.save(noId);

        Coordinate result = collection.findOne().as(Coordinate.class);
        assertThat(result).isNotNull();
        assertThat(result.lat).isEqualTo(123);
    }

    @Test
    public void shouldFailWhenMarshallerFail() throws Exception {

        try {
            collection.save(new ErrorObject());
            Assert.fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Unable to save object");
        }
    }

    @Test
    public void canSaveWithCompositeKey() {

        MapReduceData aggregate = new MapReduceData("group", new Date(), 1);

        collection.save(aggregate);
    }
}
