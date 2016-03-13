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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.model.Friend;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WriteConcernTest {

    DBCollection mockedDBCollection = mock(DBCollection.class);
    private JongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = new JongoCollection(mockedDBCollection, new JacksonMapper.Builder().build());
    }

    @Test
    public void shouldUseDefaultDriverWriteConcern() throws Exception {

        Friend john = new Friend("John");

        collection.save(john);

        verify(mockedDBCollection).save(any(DBObject.class), isNull(WriteConcern.class));
    }

    @Test
    public void canSaveWithCustomWriteConcernOnCollection() throws Exception {

        Friend john = new Friend("John");

        collection.withWriteConcern(WriteConcern.ACKNOWLEDGED).save(john);

        verify(mockedDBCollection).save(any(DBObject.class), eq(WriteConcern.ACKNOWLEDGED));
    }

    @Test
    public void canInsertWithCustomWriteConcernOnCollection() throws Exception {

        collection.withWriteConcern(WriteConcern.SAFE).insert("{name : 'Abby'}");

        verify(mockedDBCollection).insert(any(DBObject.class), eq(WriteConcern.SAFE));
    }

    @Test
    public void canUpdateWithCustomWriteConcernOnCollection() throws Exception {

        collection.withWriteConcern(WriteConcern.SAFE).update("{}").upsert().with("{$set:{name:'John'}}");

        verify(mockedDBCollection).update(any(DBObject.class), any(DBObject.class), eq(true), eq(false), eq(WriteConcern.SAFE));
    }

    @Test
    public void canRemoveWithCustomWriteConcernOnCollection() throws Exception {

        collection.withWriteConcern(WriteConcern.SAFE).remove();

        verify(mockedDBCollection).remove(any(DBObject.class), eq(WriteConcern.SAFE));
    }

}
