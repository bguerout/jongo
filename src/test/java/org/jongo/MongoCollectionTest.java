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

import org.jongo.model.Coordinate3D;
import org.jongo.model.Poi;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
    public void canCountEntities() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address, lat, lng));
        mongoCollection.save(new Poi(null, 4, 1));

        /* then */
        assertThat(mongoCollection.count(addressExists)).isEqualTo(1);
        assertThat(mongoCollection.count("{'coordinate.lat': {$exists:true}}")).isEqualTo(2);
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
    @Ignore
    public void canFindInheritedEntity() throws IOException {
        mongoCollection.save(new Poi(id, lat, lng, alt));
        Poi poi = mongoCollection.findOne("{_id: #}", id).as(Poi.class);
        assertThat(poi.coordinate).isInstanceOf(Coordinate3D.class);
    }

    @Test
    public void canGetCollectionName() throws Exception {
        assertThat(mongoCollection.getName()).isEqualTo("users");
    }


}
