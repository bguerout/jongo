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

import com.mongodb.*;
import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.jongo.model.Coordinate;
import org.jongo.model.ExternalFriend;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InsertTest extends JongoTestCase {

    private MongoCollection collection;
    @Captor
    private org.mockito.ArgumentCaptor<DBObject> captor;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canInsert() throws Exception {

        collection.insert("{name : 'Abby'}");

        assertThat(collection.count("{name : 'Abby'}")).isEqualTo(1);
    }

    @Test
    public void canInsertPojo() throws Exception {

        Friend friend = new Friend("John");

        collection.insert(friend);

        Friend result = collection.findOne("{name:'John'}").as(Friend.class);
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    public void canInsertPojos() throws Exception {

        Friend friend = new Friend("John");
        Friend friend2 = new Friend("Robert");

        collection.insert(friend, friend2);

        assertThat(collection.count("{name:'John'}")).isEqualTo(1);
        assertThat(collection.count("{name:'Robert'}")).isEqualTo(1);
    }

    @Test
    public void canInsertWithParameters() throws Exception {

        collection.insert("{name : #}", "Abby");

        assertThat(collection.count("{name : 'Abby'}")).isEqualTo(1);
    }

    @Test
    public void whenNoSpecifyShouldInsertWithCollectionWriteConcern() throws Exception {

        WriteResult writeResult = collection.withWriteConcern(WriteConcern.SAFE).insert("{name : 'Abby'}");

        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }

    @Test
    public void canInsertAnObjectWithoutId() throws Exception {

        Coordinate noId = new Coordinate(123, 1);

        collection.insert(noId);

        Coordinate result = collection.findOne().as(Coordinate.class);
        assertThat(result).isNotNull();
        assertThat(result.lat).isEqualTo(123);
    }

    @Test
    public void canInsertAPojoWithNewObjectId() throws Exception {

        ObjectId id = ObjectId.get();

        collection.withWriteConcern(WriteConcern.SAFE).insert(new Friend(id, "John"));

        assertThat(collection.count("{name : 'John'}")).isEqualTo(1);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    public void canInsertAPojoWithNotNewObjectId() throws Exception {

        ObjectId id = ObjectId.get();
        id.notNew();

        collection.withWriteConcern(WriteConcern.SAFE).insert(new Friend(id, "John"));

        Friend result = collection.findOne(id).as(Friend.class);
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    public void canInsertAPojoWithACustomId() throws Exception {

        collection.withWriteConcern(WriteConcern.SAFE).insert(new ExternalFriend(122, "value"));

        ExternalFriend result = collection.findOne("{name:'value'}").as(ExternalFriend.class);
        assertThat(result.getId()).isEqualTo(122);
    }

    @Test
    public void canOnlyInsertOnceAPojoWithObjectId() throws Exception {

        ObjectId id = ObjectId.get();
        id.notNew();

        collection.withWriteConcern(WriteConcern.SAFE).insert(new Friend(id, "John"));

        try {
            collection.withWriteConcern(WriteConcern.SAFE).insert(new Friend(id, "John"));
            Assert.fail();
        } catch (MongoException.DuplicateKey e) {
        }
    }

    @Test
    public void canOnlyInsertOnceAPojoWithACustomId() throws Exception {

        collection.withWriteConcern(WriteConcern.SAFE).insert(new ExternalFriend(122, "value"));

        try {
            collection.withWriteConcern(WriteConcern.SAFE).insert(new ExternalFriend(122, "other value"));
            Assert.fail();
        } catch (MongoException.DuplicateKey e) {
        }
    }

    @Test
    public void shouldPreventLazyDBObjectToBeDeserialized() throws Exception {

        Friend friend = new Friend(ObjectId.get(), "John");
        DBCollection mockedDBCollection = mock(DBCollection.class);
        ObjectIdUpdater objectIdUpdater = mock(ObjectIdUpdater.class);
        ObjectId deserializedOid = ObjectId.get();
        when(objectIdUpdater.getId(friend)).thenReturn(deserializedOid);
        Insert insert = new Insert(mockedDBCollection, WriteConcern.NONE, getMapper().getMarshaller(), objectIdUpdater, getMapper().getQueryFactory());

        insert.save(friend);

        verify(mockedDBCollection).save(captor.capture(), eq(WriteConcern.NONE));
        DBObject value = captor.getValue();
        assertThat(value.get("_id")).isEqualTo(deserializedOid);
    }

    @Test
    public void shouldNotPreventLazyDBObjectToBeDeserializedWhenOidIsNull() throws Exception {

        ObjectId id = ObjectId.get();
        Friend friend = new Friend(id, "John");
        DBCollection mockedDBCollection = mock(DBCollection.class);
        ObjectIdUpdater objectIdUpdater = mock(ObjectIdUpdater.class);
        ObjectId deserializedOid = null;
        when(objectIdUpdater.getId(friend)).thenReturn(deserializedOid);
        Insert insert = new Insert(mockedDBCollection, WriteConcern.NONE, getMapper().getMarshaller(), objectIdUpdater, getMapper().getQueryFactory());

        insert.save(friend);

        verify(mockedDBCollection).save(captor.capture(), eq(WriteConcern.NONE));
        DBObject value = captor.getValue();
        assertThat(value.get("_id")).isNotNull();
        assertThat(value.get("_id")).isEqualTo(id);
    }
}
