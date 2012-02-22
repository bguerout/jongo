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
import org.jongo.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.TestUtil.createEmptyCollection;
import static org.jongo.util.TestUtil.dropCollection;

public class MongoCollectionTest {

    private MongoCollection mongoCollection;
    private String address = "22 rue des murlins", id = "1";
    private int lat = 48, lng = 2, alt = 7;
    String addressExists = "{address:{$exists:true}}";

    @Before
    public void setUp() throws Exception {
        mongoCollection = createEmptyCollection("users");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canUseConditionnalOperator() throws Exception {
        /* given */
        mongoCollection.save(new Coordinate(1, 1));
        mongoCollection.save(new Coordinate(2, 1));
        mongoCollection.save(new Coordinate(3, 1));

        /* then */
        assertThat(mongoCollection.find("{lat: {$gt: 2}}").as(Coordinate.class)).hasSize(1);
        assertThat(mongoCollection.find("{lat: {$lt: 2}}").as(Coordinate.class)).hasSize(1);
        assertThat(mongoCollection.find("{lat: {$gte: 2}}").as(Coordinate.class)).hasSize(2);
        assertThat(mongoCollection.find("{lat: {$lte: 2}}").as(Coordinate.class)).hasSize(2);
        assertThat(mongoCollection.find("{lat: {$gt: 1, $lt: 3}}").as(Coordinate.class)).hasSize(1);

        assertThat(mongoCollection.find("{lat: {$ne: 2}}").as(Coordinate.class)).hasSize(2);
        assertThat(mongoCollection.find("{lat: {$in: [1,2,3]}}").as(Coordinate.class)).hasSize(3);
    }

    @Test
    public void canUseGeospacial() throws Exception {
        /* given */
        mongoCollection.save(new People("John", new Coordinate(1, 1)));
        mongoCollection.save(new People("Peter", new Coordinate(4, 4)));

        mongoCollection.ensureIndex("{ 'coordinate' : '2d'}");

        /* then */
        assertThat(mongoCollection.find("{'coordinate': {'$near': [0,0], $maxDistance: 5}}").as(People.class)).hasSize(1);
        assertThat(mongoCollection.find("{'coordinate': {'$near': [2,2], $maxDistance: 5}}").as(People.class)).hasSize(2);
        assertThat(mongoCollection.find("{'coordinate': {'$within': {'$box': [[0,0],[2,2]]}}}").as(People.class)).hasSize(1);
        assertThat(mongoCollection.find("{'coordinate': {'$within': {'$center': [[0,0],5]}}}").as(People.class)).hasSize(1);
    }

    @Test
    public void canUpdateEntity() throws Exception {
        /* given */
        String id = mongoCollection.save(new People("John", "21 Jump Street"));
        Iterator<People> peoples = mongoCollection.find("{name: 'John'}").as(People.class).iterator();
        People people = peoples.next();
        people.setAddress("new address");
        mongoCollection.save(people);

        /* when */
        peoples = mongoCollection.find("{name: 'John'}").as(People.class).iterator();

        /* then */
        people = peoples.next();
        assertThat(people.getId()).isEqualTo(new ObjectId(id));
        assertThat(people.getAddress()).isEqualTo("new address");
    }

    @Test
    public void canUpdateQuery() throws Exception {
        /* given */
        mongoCollection.save(new People("John"));
        mongoCollection.save(new People("Peter"));

        /* when */
        mongoCollection.update("{name:'John'}", "{$unset:{name:1}}");

        /* then */
        Iterator<People> peoples = mongoCollection.find("{name:{$exists:true}}").as(People.class).iterator();
        assertThat(peoples).hasSize(1);
    }

    @Test
    public void canRemoveQuery() throws Exception {
        /* given */
        mongoCollection.save(new People("John"));
        mongoCollection.save(new People("Peter"));

        /* when */
        mongoCollection.remove("{name:'John'}");

        /* then */
        Iterable<People> peoples = mongoCollection.find("{}").as(People.class);
        assertThat(peoples).hasSize(1);
    }

    @Test
    public void canFindInheritedEntity() throws IOException {
        mongoCollection.save(new Fox("fantastic", "roux"));

        Animal animal = mongoCollection.findOne("{name:'fantastic'}").as(Animal.class);

        assertThat(animal).isInstanceOf(Fox.class);
    }

    @Test
    public void canGetCollectionName() throws Exception {
        assertThat(mongoCollection.getName()).isEqualTo("users");
    }
}
