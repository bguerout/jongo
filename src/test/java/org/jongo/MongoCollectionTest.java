/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteConcern;
import junit.framework.Assert;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoCollectionTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends").withWriteConcern(WriteConcern.MAJORITY);
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canUseConditionnalOperator() throws Exception {
        /* given */
        collection.save(new Coordinate(1, 1));
        collection.save(new Coordinate(2, 1));
        collection.save(new Coordinate(3, 1));

        /* then */
        assertThat(collection.find("{lat: {$gt: 2}}").as(Coordinate.class).iterator()).hasSize(1);
        assertThat(collection.find("{lat: {$lt: 2}}").as(Coordinate.class).iterator()).hasSize(1);
        assertThat(collection.find("{lat: {$gte: 2}}").as(Coordinate.class).iterator()).hasSize(2);
        assertThat(collection.find("{lat: {$lte: 2}}").as(Coordinate.class).iterator()).hasSize(2);
        assertThat(collection.find("{lat: {$gt: 1, $lt: 3}}").as(Coordinate.class).iterator()).hasSize(1);

        assertThat(collection.find("{lat: {$ne: 2}}").as(Coordinate.class).iterator()).hasSize(2);
        assertThat(collection.find("{lat: {$in: [1,2,3]}}").as(Coordinate.class).iterator()).hasSize(3);
    }

    @Test
    public void createIndexWithUniqueAsOption() {
        collection.ensureIndex("{name: 1}", "{unique: true}");
        collection.save(new Friend("John"));

        try {
            collection.save(new Friend("John"));
            Assert.fail();
        } catch (DuplicateKeyException e) {
        }
    }

    @Test
    public void canCreateGeospacialIndex() throws Exception {
        /* given */
        collection.save(new Friend("John", new Coordinate(1, 1)));
        collection.save(new Friend("Peter", new Coordinate(4, 4)));

        collection.ensureIndex("{ 'coordinate' : '2d'},{ 'coordinate' : '2d'}");

        /* then */
        assertThat(collection.find("{'coordinate': {'$near': [0,0], $maxDistance: 5}}").as(Friend.class).iterator()).hasSize(1);
        assertThat(collection.find("{'coordinate': {'$near': [2,2], $maxDistance: 5}}").as(Friend.class).iterator()).hasSize(2);
        assertThat(collection.find("{'coordinate': {'$within': {'$box': [[0,0],[2,2]]}}}").as(Friend.class).iterator()).hasSize(1);
        assertThat(collection.find("{'coordinate': {'$within': {'$center': [[0,0],5]}}}").as(Friend.class).iterator()).hasSize(1);
    }

    @Test
    public void canDropIndex() {

        //given
        collection.ensureIndex("{name: 1}", "{unique: true}");
        collection.save(new Friend("John"));

        //when
        collection.dropIndex("{name: 1}");

        //then
        collection.save(new Friend("John"));
    }

    @Test
    public void canDropIndexes() {

        //given
        collection.ensureIndex("{name: 1}", "{unique: true}");
        collection.ensureIndex("{way: 1}", "{unique: true}");
        collection.save(new Friend("John", "way"));

        //when
        collection.dropIndexes();

        //then
        collection.save(new Friend("John", "way"));
    }

    @Test
    public void canGetCollectionName() throws Exception {
        assertThat(collection.getName()).isEqualTo("friends");
    }
}
