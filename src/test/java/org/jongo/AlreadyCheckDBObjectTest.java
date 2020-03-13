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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AlreadyCheckDBObjectTest extends JongoTestBase {

    ArgumentCaptor<DBObject> captor = ArgumentCaptor.forClass(DBObject.class);
    DBCollection mockedDBCollection = mock(DBCollection.class);
    ObjectIdUpdater objectIdUpdater = mock(ObjectIdUpdater.class);

    @Test
    public void shouldPreventLazyDBObjectToBeDeserialized() throws Exception {

        Friend friend = new Friend(ObjectId.get(), "John");
        ObjectId deserializedOid = ObjectId.get();
        when(objectIdUpdater.getId(friend)).thenReturn(deserializedOid);
        Insert insert = new Insert(mockedDBCollection, WriteConcern.UNACKNOWLEDGED, getMapper().getMarshaller(), objectIdUpdater, getMapper().getQueryFactory());

        insert.save(friend);

        verify(mockedDBCollection).save(captor.capture(), eq(WriteConcern.UNACKNOWLEDGED));
        DBObject value = captor.getValue();
        assertThat(value.get("_id")).isEqualTo(deserializedOid);
    }

    @Test
    public void shouldNotPreventLazyDBObjectToBeDeserializedWhenOidIsNull() throws Exception {

        ObjectId id = ObjectId.get();
        Friend friend = new Friend(id, "John");
        when(objectIdUpdater.getId(friend)).thenReturn(null);
        Insert insert = new Insert(mockedDBCollection, WriteConcern.UNACKNOWLEDGED, getMapper().getMarshaller(), objectIdUpdater, getMapper().getQueryFactory());

        insert.save(friend);

        verify(mockedDBCollection).save(captor.capture(), eq(WriteConcern.UNACKNOWLEDGED));
        DBObject value = captor.getValue();
        assertThat(value.get("_id")).isNotNull();
        assertThat(value.get("_id")).isEqualTo(id);
    }
}
