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

import java.io.IOException;

import org.jongo.model.Animal;
import org.jongo.model.Coordinate;
import org.jongo.model.Fox;
import org.jongo.model.People;
import org.junit.Rule;
import org.junit.Test;

public class MongoCollectionTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canUseConditionnalOperator() throws Exception {
        /* given */
        collection.save(new Coordinate(1, 1));
        collection.save(new Coordinate(2, 1));
        collection.save(new Coordinate(3, 1));

        /* then */
        assertThat(collection.find("{lat: {$gt: 2}}").as(Coordinate.class)).hasSize(1);
        assertThat(collection.find("{lat: {$lt: 2}}").as(Coordinate.class)).hasSize(1);
        assertThat(collection.find("{lat: {$gte: 2}}").as(Coordinate.class)).hasSize(2);
        assertThat(collection.find("{lat: {$lte: 2}}").as(Coordinate.class)).hasSize(2);
        assertThat(collection.find("{lat: {$gt: 1, $lt: 3}}").as(Coordinate.class)).hasSize(1);

        assertThat(collection.find("{lat: {$ne: 2}}").as(Coordinate.class)).hasSize(2);
        assertThat(collection.find("{lat: {$in: [1,2,3]}}").as(Coordinate.class)).hasSize(3);
    }

    @Test
    public void canUseGeospacial() throws Exception {
        /* given */
        collection.save(new People("John", new Coordinate(1, 1)));
        collection.save(new People("Peter", new Coordinate(4, 4)));

        collection.ensureIndex("{ 'coordinate' : '2d'}");

        /* then */
        assertThat(collection.find("{'coordinate': {'$near': [0,0], $maxDistance: 5}}").as(People.class)).hasSize(1);
        assertThat(collection.find("{'coordinate': {'$near': [2,2], $maxDistance: 5}}").as(People.class)).hasSize(2);
        assertThat(collection.find("{'coordinate': {'$within': {'$box': [[0,0],[2,2]]}}}").as(People.class)).hasSize(1);
        assertThat(collection.find("{'coordinate': {'$within': {'$center': [[0,0],5]}}}").as(People.class)).hasSize(1);
    }

    @Test
    public void canFindInheritedEntity() throws IOException {
        collection.save(new Fox("fantastic", "roux"));

        Animal animal = collection.findOne("{name:'fantastic'}").as(Animal.class);

        assertThat(animal).isInstanceOf(Fox.class);
        assertThat(animal.getId()).isNotNull();
    }

    @Test
    public void setEntityGeneratedId() throws IOException {
        Fox fox = new Fox("fantastic", "roux");

        collection.save(fox);

        assertThat(fox.getId()).isNotNull();
    }

    @Test
    public void canGetCollectionName() throws Exception {
        assertThat(collection.getName()).isEqualTo("users");
    }
}
