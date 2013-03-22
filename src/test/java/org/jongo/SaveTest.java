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

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.jongo.model.Coordinate;
import org.jongo.model.ExternalFriend;
import org.jongo.model.Friend;
import org.jongo.model.LinkedFriend;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class SaveTest extends JongoTestCase {

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
    public void canSavePOJO() throws Exception {

        Friend friend = new Friend("John", "22 Wall Street Avenue");

        collection.save(friend);

        assertThat(collection.count("{name:'John', address:'22 Wall Street Avenue'}")).isEqualTo(1);
    }

    @Test
    public void whenNoSpecifyShouldSaveWithCollectionWriteConcern() throws Exception {

        Friend friend = new Friend("John", "22 Wall Street Avenue");

        WriteResult writeResult = collection.save(friend);

        assertThat(writeResult.getLastConcern()).isEqualTo(collection.getDBCollection().getWriteConcern());
    }

    @Test
    public void canSaveWithWriteConcern() throws Exception {

        Friend friend = new Friend("John", "22 Wall Street Avenue");

        WriteResult writeResult = collection.withWriteConcern(WriteConcern.SAFE).save(friend);

        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }

    @Test
    public void canUpdateAlreadySavedEntity() throws Exception {

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
    public void canSaveAnObjectWithAnObjectId() throws Exception {

        Friend john = new Friend(new ObjectId("47cc67093475061e3d95369d"), "John");

        collection.save(john);

        Friend result = collection.findOne(new ObjectId("47cc67093475061e3d95369d")).as(Friend.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void canSaveAnObjectWithACustomTypeId() throws Exception {

        ExternalFriend john = new ExternalFriend(999, "Robert");

        collection.save(john);

        ExternalFriend result = collection.findOne().as(ExternalFriend.class);
        assertThat(result).isNotNull();
        assertThat(result.get_id()).isEqualTo(999);
    }

    @Test
    public void canInsertAnObjectWithoutId() throws Exception {

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
    public void shouldUpdateIdField() throws IOException {

        Friend robert = new Friend("Robert", "21 Jump Street");

        collection.save(robert);

        Friend result = collection.findOne().as(Friend.class);
        assertThat(robert.getId()).isNotNull();
        assertThat(result.getId()).isEqualTo(robert.getId());
    }

    @Test
    public void shouldNotChangeOtherObjectIdField() throws IOException {

        ObjectId relationId = new ObjectId();
        LinkedFriend friend = new LinkedFriend(relationId);

        collection.save(friend);

        assertThat(friend.getRelationId()).isNotEqualTo(friend.getId());
        assertThat(friend.getRelationId()).isEqualTo(relationId);
    }

}
