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
import static org.jongo.util.TestUtil.createEmptyCollection;
import static org.jongo.util.TestUtil.dropCollection;

import java.io.IOException;
import java.util.Iterator;

import org.bson.types.ObjectId;
import org.jongo.model.Animal;
import org.jongo.model.Fox;
import org.jongo.model.Poi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        mongoCollection.save(new Poi(address, 1, 1));
        mongoCollection.save(new Poi(address, 2, 1));
        mongoCollection.save(new Poi(address, 3, 1));

        /* then */
        assertThat(mongoCollection.find("{coordinate.lat: {$gt: 2}}").as(Poi.class)).hasSize(1);
        assertThat(mongoCollection.find("{coordinate.lat: {$lt: 2}}").as(Poi.class)).hasSize(1);
        assertThat(mongoCollection.find("{coordinate.lat: {$gte: 2}}").as(Poi.class)).hasSize(2);
        assertThat(mongoCollection.find("{coordinate.lat: {$lte: 2}}").as(Poi.class)).hasSize(2);
        assertThat(mongoCollection.find("{coordinate.lat: {$gt: 1, $lt: 3}}").as(Poi.class)).hasSize(1);

        assertThat(mongoCollection.find("{coordinate.lat: {$ne: 2}}").as(Poi.class)).hasSize(2);
        assertThat(mongoCollection.find("{coordinate.lat: {$in: [1,2,3]}}").as(Poi.class)).hasSize(3);
    }

    @Test
    public void canUseGeospacial() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address, 1, 1));
        mongoCollection.save(new Poi(address, 4, 4));

        mongoCollection.ensureIndex("{ 'coordinate' : '2d'}");

        /* then */
        assertThat(mongoCollection.find("{'coordinate': {'$near': [0,0], $maxDistance: 5}}").as(Poi.class)).hasSize(1);
        assertThat(mongoCollection.find("{'coordinate': {'$near': [2,2], $maxDistance: 5}}").as(Poi.class)).hasSize(2);
        assertThat(mongoCollection.find("{'coordinate': {'$within': {'$box': [[0,0],[2,2]]}}}").as(Poi.class)).hasSize(1);
        assertThat(mongoCollection.find("{'coordinate': {'$within': {'$center': [[0,0],5]}}}").as(Poi.class)).hasSize(1);
    }

    @Test
    public void canUpdateEntity() throws Exception {
        /* given */
        mongoCollection.save(new Poi(id, address));
        Iterator<Poi> pois = mongoCollection.find("{_id: '1'}").as(Poi.class);
        Poi poi = pois.next();
        poi.address = null;
        mongoCollection.save(poi);

        /* when */
        pois = mongoCollection.find("{_id: '1'}").as(Poi.class);

        /* then */
        poi = pois.next();
        assertThat(poi.id).isEqualTo(id);
        assertThat(poi.address).isNull();
    }

    @Test
    public void canUpdateQuery() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address));
        mongoCollection.save(new Poi("9 rue des innocents"));

        /* when */
        mongoCollection.update("{address:'9 rue des innocents'}", "{$unset:{address:1}}");

        /* then */
        Iterator<Poi> pois = mongoCollection.find(addressExists).as(Poi.class);
        assertThat(pois).hasSize(1);
    }

    @Test
    public void canRemoveQuery() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address));
        mongoCollection.save(new Poi("9 rue des innocents"));

        /* when */
        mongoCollection.remove("{address:'9 rue des innocents'}");

        /* then */
        Iterator<Poi> pois = mongoCollection.find(addressExists).as(Poi.class);
        assertThat(pois).hasSize(1);
    }

    @Test
    public void canFindInheritedEntity() throws IOException {
        String dogId = mongoCollection.save(new Fox("fantastic", "roux"));
        Animal animal = mongoCollection.findOne(new ObjectId(dogId)).as(Animal.class);
        assertThat(animal).isInstanceOf(Fox.class);
    }

    @Test
    public void canGetCollectionName() throws Exception {
        assertThat(mongoCollection.getName()).isEqualTo("users");
    }

}
