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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.model.LinkedFriend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class SaveTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canSavePOJO() throws Exception {
        /* given */
        Friend friend = new Friend("John", "22 Wall Street Avenue");
        collection.save(friend);

        assertThat(collection.count("{}")).isEqualTo(1);
        assertThat(friend.getId()).isNotNull();
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

        WriteResult writeResult = collection.save(friend, WriteConcern.SAFE);

        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }

    @Test
    public void canModifyAlreadySavedEntity() throws Exception {
        /* given */
        Friend john = new Friend("John", "21 Jump Street");
        collection.save(john);
        john.setAddress("new address");

        /* when */
        collection.save(john);

        /* then */
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

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMarshalledJsonIsInvalid() throws Exception {

        Marshaller marshaller = mock(Marshaller.class);
        Unmarshaller unmarshaller = mock(Unmarshaller.class);
        when(marshaller.marshall(anyObject())).thenReturn("invalid");
        Save save = new Save(collection.getDBCollection(), marshaller, unmarshaller, new Object());

        save.execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMarshallerFail() throws Exception {

        Marshaller marshaller = mock(Marshaller.class);
        Unmarshaller unmarshaller = mock(Unmarshaller.class);
        when(marshaller.marshall(anyObject())).thenThrow(new RuntimeException());
        Save save = new Save(collection.getDBCollection(), marshaller, unmarshaller, new Object());

        save.execute();
    }

    @Test
    public void canSetEntityGeneratedObjectIdAndRestrictedVisibility() throws IOException {

        Friend robert = new Friend("Robert", "21 Jump Street");

        collection.save(robert);

        assertThat(robert.getId()).isNotNull();
    }

    @Test
    public void canSetEntityGeneratedObjectIdOnSuperType() throws IOException {

        Fox fox = new Fox("fantastic", "roux");

        collection.save(fox);

        assertThat(fox.getId()).isNotNull();
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
