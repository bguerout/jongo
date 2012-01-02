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

package com.jongo;

import com.jongo.model.Coordinate;
import com.jongo.model.Poi;
import com.mongodb.MongoException;
import org.bson.BSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class MongoCollectionTest {

    private MongoCollection mongoCollection;

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        mongoCollection = new MongoCollection("jongo", "poi");
        mongoCollection.drop();
    }

    String address = "22 rue des murlins", id = "1";
    int lat = 48, lng = 2;

    @Test
    public void canFindEntityOnAddress() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{address:{$exists:true}}", Poi.class);

        /* then */
        Poi result = results.next();
        assertThat(result.address).isEqualTo(address);
        assertThat(result.id).isNotNull();
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canFindEntityOnId() throws Exception {
        /* given */
        mongoCollection.save(new Poi(id, address));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{_id: '1'}", Poi.class);

        /* then */
        Poi result = results.next();
        assertThat(result.address).isEqualTo(address);
        assertThat(result.id).isEqualTo(id);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canExecuteQueryAndMapResult() throws Exception {

        /* given */
        mongoCollection.save(new Poi(address));// TODO save method must return
        // generated id

        /* when */
        String id = mongoCollection.findOne("{address:{$exists:true}}", new ResultMapper<String>() {
            public String map(BSONObject result) {
                return result.get(MongoCollection.MONGO_ID).toString();
            }
        });

        /* then */
        assertThat(id).isNotNull();
    }

    @Test
    public void canFindEntityUsingSubProperty() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address, lat, lng));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{'coordinate.lat':48}", Poi.class);

        /* then */
        assertThat(results.next().coordinate.lat).isEqualTo(lat);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canSortEntities() throws Exception {
        /* given */
        mongoCollection.save(new Poi("23 rue des murlins"));
        mongoCollection.save(new Poi("21 rue des murlins"));
        mongoCollection.save(new Poi("22 rue des murlins"));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{'$query':{}, '$orderby':{'address':1}}", Poi.class);

        /* then */
        assertThat(results.next().address).isEqualTo("21 rue des murlins");
        assertThat(results.next().address).isEqualTo("22 rue des murlins");
        assertThat(results.next().address).isEqualTo("23 rue des murlins");
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canFilterDistinctEntities() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address));
        mongoCollection.save(new Poi(address));
        mongoCollection.save(new Poi("23 rue des murlins"));

        /* when */
        Iterator<String> addresses = mongoCollection.distinct("address", "", String.class);

        /* then */
        assertThat(addresses.next()).isEqualTo(address);
        assertThat(addresses.next()).isEqualTo("23 rue des murlins");
        assertThat(addresses.hasNext()).isFalse();
    }

    @Test
    public void canFilterDistinctEntitiesOnTypedProperty() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address, lat, lng));
        mongoCollection.save(new Poi(address, lat, lng));
        mongoCollection.save(new Poi(address, 4, 1));

        /* when */
        Iterator<Coordinate> coordinates = mongoCollection.distinct("coordinate", "", Coordinate.class);

        /* then */
        Coordinate first = coordinates.next();
        assertThat(first.lat).isEqualTo(lat);
        assertThat(first.lng).isEqualTo(lng);
        Coordinate second = coordinates.next();
        assertThat(second.lat).isEqualTo(4);
        assertThat(second.lng).isEqualTo(1);
        assertThat(coordinates.hasNext()).isFalse();
    }

    @Test
    public void canFilterDistinctEntitiesWithQuery() throws Exception {
        /* given */
        mongoCollection.save(new Poi(address, lat, lng));
        mongoCollection.save(new Poi(address, lat, lng));
        mongoCollection.save(new Poi(null, 4, 1));

        /* when */
        Iterator<Coordinate> coordinates = mongoCollection.distinct("coordinate", "{address:{$exists:true}}",
                Coordinate.class);

        /* then */
        Coordinate first = coordinates.next();
        assertThat(first.lat).isEqualTo(lat);
        assertThat(first.lng).isEqualTo(lng);
        assertThat(coordinates.hasNext()).isFalse();
    }
}
