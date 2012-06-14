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

import java.util.Iterator;

import org.bson.types.ObjectId;
import org.jongo.model.People;
import org.junit.Rule;
import org.junit.Test;

public class FindByObjectIdTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullObjectId() throws Exception {
        collection.findOne((ObjectId) null);
    }

    @Test
    public void canFindOneWithObjectId() throws Exception {
        /* given */
        String id = collection.save(newPeople());

        ObjectId objectId = new ObjectId(id);
        People foundPeople = collection.findOne(objectId).as(People.class);

        /* then */
        assertThat(foundPeople).isNotNull();
        assertThat(foundPeople.getId()).isEqualTo(id);
    }

    @Test
    public void canFindOne() throws Exception {
        /* given */
        String id = collection.save(newPeople());

        People foundPeople = collection.findOne("{_id:{$oid:#}}", id).as(People.class);

        /* then */
        assertThat(foundPeople).isNotNull();
        assertThat(foundPeople.getId()).isEqualTo(id);
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        String id = collection.save(newPeople());

        Iterator<People> users = collection.find("{_id:{$oid:#}}", id).as(People.class).iterator();

        /* then */
        assertThat(users).isNotNull();
        assertThat(users.next().getId()).isEqualTo(id);
    }
}
