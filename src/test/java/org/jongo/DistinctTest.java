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

import java.util.Iterator;

import org.jongo.model.Coordinate;
import org.jongo.model.People;
import org.junit.Rule;
import org.junit.Test;

public class DistinctTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    private String wallStreetAvenue = "22 Wall Street Avenue";

    @Test
    public void distinctOnStringEntities() throws Exception {
        /* given */
        collection.save(new People("John", wallStreetAvenue));
        collection.save(new People("Smith", wallStreetAvenue));
        collection.save(new People("Peter", "24 Wall Street Avenue"));

        /* when */
        Iterator<String> addresses = collection.distinct("address").as(String.class).iterator();

        /* then */
        assertThat(addresses.next()).isEqualTo(wallStreetAvenue);
        assertThat(addresses.next()).isEqualTo("24 Wall Street Avenue");
        assertThat(addresses.hasNext()).isFalse();
    }

    @Test
    public void distinctOnIntegerEntities() throws Exception {
        /* given */
        collection.save(new People("John", new Coordinate(1, 2)));
        collection.save(new People("Peter", new Coordinate(1, 2)));
        collection.save(new People("Paul", new Coordinate(125, 72)));

        /* when */
        Iterator<Integer> addresses = collection.distinct("coordinate.lat").as(Integer.class).iterator();

        /* then */
        assertThat(addresses.next()).isEqualTo(1);
        assertThat(addresses.next()).isEqualTo(125);
        assertThat(addresses.hasNext()).isFalse();
    }

    @Test
    public void distinctOnTypedProperty() throws Exception {
        /* given */
        collection.save(new People("John", new Coordinate(1, 2)));
        collection.save(new People("Peter", new Coordinate(1, 2)));
        collection.save(new People("Paul", new Coordinate(125, 72)));

        /* when */
        Iterator<Coordinate> coordinates = collection.distinct("coordinate").as(Coordinate.class).iterator();

        /* then */
        Coordinate first = coordinates.next();
        assertThat(first.lat).isEqualTo(1);
        assertThat(first.lng).isEqualTo(2);
        Coordinate second = coordinates.next();
        assertThat(second.lat).isEqualTo(125);
        assertThat(second.lng).isEqualTo(72);
        assertThat(coordinates.hasNext()).isFalse();
    }

    @Test
    public void distinctWithQuery() throws Exception {
        /* given */
        collection.save(new People("John", new Coordinate(1, 2)));
        collection.save(new People("Peter", new Coordinate(1, 2)));
        String emptyName = null;
        collection.save(new People(emptyName, new Coordinate(125, 72)));

        /* when */
        Iterator<Coordinate> coordinates = collection.distinct("coordinate").query("{name:{$exists:true}}").as(Coordinate.class).iterator();

        /* then */
        Coordinate first = coordinates.next();
        assertThat(first.lat).isEqualTo(1);
        assertThat(first.lng).isEqualTo(2);
        assertThat(coordinates.hasNext()).isFalse();
    }

    @Test
    public void distinctWithParameterizedQuery() throws Exception {
        /* given */
        collection.save(new People("John", new Coordinate(1, 2)));
        collection.save(new People("Peter", new Coordinate(3, 4)));

        /* when */
        Iterator<Coordinate> coordinates = collection.distinct("coordinate").query("{name:#}", "Peter").as(Coordinate.class).iterator();

        /* then */
        Coordinate first = coordinates.next();
        assertThat(first.lat).isEqualTo(3);
        assertThat(first.lng).isEqualTo(4);
        assertThat(coordinates.hasNext()).isFalse();
    }
}
