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

import org.jongo.model.Coordinate;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class FindTest extends JongoTestCase {

    private MongoCollection collection;
    private People people;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
        people = new People("John", "22 Wall Street Avenue");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        String id = collection.save(people);

        /* when */
        Iterator<People> users = collection.find("{address:{$exists:true}}").as(People.class).iterator();

        /* then */
        assertThat(users.next().getId()).isEqualTo(id);
        assertThat(users.hasNext()).isFalse();
    }

    @Test
    public void canFindWithEmptySelector() throws Exception {
        /* given */
        String id = collection.save(this.people);
        String id2 = collection.save(new People("Smith", "23 Wall Street Avenue"));
        String id3 = collection.save(new People("Peter", "24 Wall Street Avenue"));

        /* when */
        Iterator<People> users = collection.find("{}").as(People.class).iterator();

        /* then */
        People people = users.next();
        assertThat(people.getId()).isEqualTo(id);
        assertThat(people.getName()).isEqualTo("John");
        assertThat(users.next().getId()).isEqualTo(id2);
        assertThat(users.next().getId()).isEqualTo(id3);
        assertThat(users.hasNext()).isFalse();
    }

    @Test
    public void canFindUsingSubProperty() throws Exception {
        /* given */
        collection.save(new People("John", new Coordinate(2, 31)));

        /* when */
        Iterator<People> results = collection.find("{'coordinate.lat':2}").as(People.class).iterator();

        /* then */
        assertThat(results.next().getCoordinate().lat).isEqualTo(2);
        assertThat(results.hasNext()).isFalse();
    }

}
