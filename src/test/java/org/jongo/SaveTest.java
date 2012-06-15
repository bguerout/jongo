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
import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

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
        People people = new People("John", "22 Wall Street Avenue");
        collection.save(people);

        assertThat(collection.count("{}")).isEqualTo(1);
        assertThat(people.getId()).isNotNull();
    }

    @Test
    public void whenNoSpecifyShouldSaveWithCollectionWriteConcern() throws Exception {

        People people = new People("John", "22 Wall Street Avenue");

        WriteResult writeResult = collection.save(people);

        assertThat(writeResult.getLastConcern()).isEqualTo(collection.getDBCollection().getWriteConcern());
    }

    @Test
    public void canSaveWithWriteConcern() throws Exception {

        People people = new People("John", "22 Wall Street Avenue");

        WriteResult writeResult = collection.save(people, WriteConcern.SAFE);

        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }

    @Test
    public void canModifyAlreadySavedEntity() throws Exception {
        /* given */
        People john = new People("John", "21 Jump Street");
        collection.save(john);
        john.setAddress("new address");

        /* when */
        collection.save(john);

        /* then */
        ObjectId johnId = john.getId();
        People johnWithNewAddress = collection.findOne(johnId).as(People.class);
        assertThat(johnWithNewAddress.getId()).isEqualTo(johnId);
        assertThat(johnWithNewAddress.getAddress()).isEqualTo("new address");
    }

    @Test
    public void canSaveAnObjectWithAnObjectId() throws Exception {

        People john = new People(new ObjectId("47cc67093475061e3d95369d"), "John");

        collection.save(john);

        People result = collection.findOne(new ObjectId("47cc67093475061e3d95369d")).as(People.class);
        assertThat(result).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMarshalledJsonIsInvalid() throws Exception {

        Marshaller marshaller = mock(Marshaller.class);
        when(marshaller.marshall(anyObject())).thenReturn("invalid");
        Save save = new Save(collection.getDBCollection(), marshaller, new Object());

        save.execute();
    }
}
