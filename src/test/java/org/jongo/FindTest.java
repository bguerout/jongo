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

import org.jongo.model.Coordinate;
import org.jongo.model.People;
import org.junit.Rule;
import org.junit.Test;

public class FindTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canFind() throws Exception {
        /* given */
        String id = collection.save(newPeople());

        /* when */
        Iterator<People> users = collection.find("{address:{$exists:true}}").as(People.class).iterator();

        /* then */
        assertThat(users.next().getId()).isEqualTo(id);
        assertThat(users.hasNext()).isFalse();
    }

    @Test
    public void canFindWithEmptySelector() throws Exception {
        /* given */
        String id = collection.save(newPeople());
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
