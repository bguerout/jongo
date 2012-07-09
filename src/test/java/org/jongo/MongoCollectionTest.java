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

import org.jongo.model.Animal;
import org.jongo.model.Coordinate;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.WriteConcern;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class MongoCollectionTest extends JongoTestCase {

    private MongoCollection mongoCollection;

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
        mongoCollection.save(new Friend("John", new Coordinate(1, 1)));
        mongoCollection.save(new Friend("Peter", new Coordinate(4, 4)));

        mongoCollection.ensureIndex("{ 'coordinate' : '2d'}");

        /* then */
        assertThat(mongoCollection.find("{'coordinate': {'$near': [0,0], $maxDistance: 5}}").as(Friend.class)).hasSize(1);
        assertThat(mongoCollection.find("{'coordinate': {'$near': [2,2], $maxDistance: 5}}").as(Friend.class)).hasSize(2);
        assertThat(mongoCollection.find("{'coordinate': {'$within': {'$box': [[0,0],[2,2]]}}}").as(Friend.class)).hasSize(1);
        assertThat(mongoCollection.find("{'coordinate': {'$within': {'$center': [[0,0],5]}}}").as(Friend.class)).hasSize(1);
    }

    @Test
    public void canFindInheritedEntity() throws IOException {
        mongoCollection.save(new Fox("fantastic", "roux"));

        Animal animal = mongoCollection.findOne("{name:'fantastic'}").as(Animal.class);

        assertThat(animal).isInstanceOf(Fox.class);
        assertThat(animal.getId()).isNotNull();
    }

    @Test
    public void canGetCollectionName() throws Exception {
        assertThat(mongoCollection.getName()).isEqualTo("users");
    }
    
    @Test(expected=DuplicateKey.class)
    public void createUniqueIndex() {
    	mongoCollection.ensureIndex("{name: 1}, {unique: true}");
    	mongoCollection.save(new Friend("John"), WriteConcern.SAFE);
        mongoCollection.save(new Friend("John"), WriteConcern.SAFE);
    }
}
