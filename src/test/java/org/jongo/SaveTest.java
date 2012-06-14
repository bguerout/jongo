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
import static org.jongo.JongoTest.collection;
import static org.jongo.JongoTest.newPeople;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;
import org.jongo.model.People;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.mongodb.WriteConcern;

public class SaveTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canSavePOJO() throws Exception {
        /* given */
        String id = collection.save(newPeople());

        assertThat(collection.count("{}")).isEqualTo(1);
        assertThat(id).isNotNull();
    }

    @Test
    @Ignore
    public void whenNoSpecifyShouldSaveWithCollectionWriteConcern() throws Exception {

        collection.getDBCollection().setWriteConcern(WriteConcern.JOURNAL_SAFE);
        WriteConcern writeConcern = spy(collection.getDBCollection().getWriteConcern());

        collection.save(newPeople(), writeConcern);

        verify(writeConcern).callGetLastError();
    }

    @Test
    @Ignore
    public void canSaveWithWriteConcern() throws Exception {

        WriteConcern writeConcern = spy(WriteConcern.SAFE);

        collection.save(newPeople(), writeConcern);

        verify(writeConcern).callGetLastError();
    }

    @Test
    public void canSavedAnObjectWithAnObjectId() throws Exception {

        ObjectId id = new ObjectId();
        People john = new People(id, "John");

        String savedId = collection.save(john);

        assertThat(savedId).isEqualTo(id.toString());
    }

    @Test
    public void canModifyAlreadySavedEntity() throws Exception {
        /* given */
        String idAsString = collection.save(new People("John", "21 Jump Street"));
        ObjectId id = new ObjectId(idAsString);
        People people = collection.findOne(id).as(People.class);
        people.setAddress("new address");

        /* when */
        collection.save(people);

        /* then */
        people = collection.findOne(id).as(People.class);
        assertThat(people.getId()).isEqualTo(id);
        assertThat(people.getAddress()).isEqualTo("new address");
    }

    @Test
    public void canSaveAnObjectWithAnObjectId() throws Exception {

        People john = new People(new ObjectId("47cc67093475061e3d95369d"), "John");

        String id = collection.save(john);

        People result = collection.findOne(new ObjectId(id)).as(People.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("47cc67093475061e3d95369d");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMarshalledJsonIsInvalid() throws Exception {

        Marshaller marshaller = mock(Marshaller.class);
        when(marshaller.marshall(anyObject())).thenReturn("invalid");
        Save save = new Save(collection.getDBCollection(), marshaller, new Object());

        save.execute();
    }
}
