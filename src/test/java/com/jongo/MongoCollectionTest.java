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

    @Test
    public void canFindEntity() throws Exception {
        /* given */
        String address = "22 rue des murlins";
        mongoCollection.save(new Poi(address));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{address:{$exists:true}}", Poi.class);

        /* then */
        assertThat(results.next().address).isEqualTo(address);
        assertThat(results.hasNext()).isFalse();
    }

    @Test
    public void canExecuteQueryAndMapResult() throws Exception {

        /* given */
        String address = "22 rue des murlins";
        mongoCollection.save(new Poi(address));//TODO save method must return generated id

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
        String address = "22 rue des murlins";
        int lat = 48, lng = 2;
        mongoCollection.save(new Poi(address, lat, lng));

        /* when */
        Iterator<Poi> results = mongoCollection.find("{'coordinate.lat':48}", Poi.class);

        /* then */
        assertThat(results.next().coordinate.lat).isEqualTo(lat);
        assertThat(results.hasNext()).isFalse();
    }
}
