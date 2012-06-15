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

import org.bson.types.ObjectId;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class FindByObjectIdTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullObjectId() throws Exception {
        collection.findOne((ObjectId) null);
    }

    @Test
    public void canFindOneWithObjectId() throws Exception {
        /* given */
        People john = new People("John", "22 Wall Street Avenue");
        collection.save(john);

        People foundPeople = collection.findOne(john.getId()).as(People.class);

        /* then */
        assertThat(foundPeople).isNotNull();
        assertThat(foundPeople.getId()).isEqualTo(john.getId());
    }

    @Test
    public void canFindOneWithOid() throws Exception {
        /* given */
        People john = new People("John", "22 Wall Street Avenue");
        collection.save(john);
        String idAsString = john.getId().toString();

        People foundPeople = collection.findOne("{_id:{$oid:#}}", idAsString).as(People.class);

        /* then */
        assertThat(foundPeople).isNotNull();
        assertThat(foundPeople.getId()).isEqualTo(john.getId());
    }

    @Test
    public void canFindWithOid() throws Exception {
        /* given */
        People john = new People("John", "22 Wall Street Avenue");
        collection.save(john);
        String idAsString = john.getId().toString();

        Iterator<People> users = collection.find("{_id:{$oid:#}}", idAsString).as(People.class).iterator();

        /* then */
        assertThat(users).isNotNull();
        assertThat(users.next().getId()).isEqualTo(john.getId());
    }
}
