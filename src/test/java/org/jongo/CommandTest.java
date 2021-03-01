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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import org.assertj.core.api.Condition;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandTest extends JongoTestBase {

    private Jongo jongo;
    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        jongo = getJongo();
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canRunACommand() throws Exception {
        DBObject result = jongo.runCommand("{ ping: 1 }").map(new RawResultHandler<DBObject>());

        assertThat(result).isNotNull();
        assertThat(result.get("ok")).isEqualTo(1.0);
    }

    @Test
    public void canRunAnEvalCommand() throws Exception {

        collection.insert("{doc:1}");

        String js = "function() { return db.friends.findOne()}";
        DBObject result = jongo.runCommand("{ eval: # }", js).map(new RawResultHandler<DBObject>());

        assertThat(result).isNotNull();
        DBObject retval = (DBObject) result.get("retval");
        assertThat(retval.get("doc")).isEqualTo(1);
    }

    @Test
    public void canRunACommandWithParameter() throws Exception {

        collection.withWriteConcern(WriteConcern.MAJORITY).insert("{test:1}");

        DBObject result = jongo.runCommand("{ count: #}", "friends").map(new RawResultHandler<DBObject>());

        Number n = (Number) result.get("n");
        assertThat(n.intValue()).isEqualTo(1);
    }

    @Test
    public void canRunAGeoNearCommand() throws Exception {

        MongoCollection safeCollection = collection.withWriteConcern(WriteConcern.MAJORITY);
        safeCollection.insert("{loc:{lat:48.690833,lng:9.140556}, name:'Paris'}");
        safeCollection.ensureIndex("{loc:'2d'}");

        List<Location> locations = jongo.runCommand("{ geoNear : 'friends', near : [48.690,9.140], spherical: true}")
                .throwOnError()
                .field("results")
                .as(Location.class);

        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations.get(0).dis).has(new Condition<Double>() {
            @Override
            public boolean matches(Double value) {
                return value instanceof Double && value > 1.7E-5 && value < 1.8E-5;
            }
        });
        assertThat(locations.get(0).getName()).isEqualTo("Paris");
    }

    @Test
    public void canRunAnEmptyResultCommand() throws Exception {

        List<DBObject> locations = jongo.runCommand("{ geoNear : 'friends' , near : [48.690,9.140]}")
                .field("results")
                .map(new RawResultHandler<DBObject>());

        assertThat(locations).isEmpty();
    }

    @Test
    public void canRunACommandAs() throws Exception {
        Validate status = jongo.runCommand("{ validate: 1 }").as(Validate.class);

        assertThat(status.errmsg).isNotNull();
        assertThat(status.ok).isEqualTo("0.0");
    }

    @Test
    public void canRunInvalidCommand() throws Exception {
        Validate status = jongo.runCommand("{forceerror:1}").as(Validate.class);

        assertThat(status.ok).isEqualTo("0.0");
    }

    @Test
    public void mustForceExceptionToBeThrownOnInvalidCommand() throws Exception {
        try {
            jongo.runCommand("{forceerror:1}").throwOnError().as(Validate.class);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("errmsg");
        }
    }

    private static class Validate {
        String ok, errmsg;
    }


    private static class Location {

        double dis;

        /**
         * Real Location document is contained into 'obj' property
         * Jackson doesn't support nested mapping. see http://jira.codehaus.org/browse/JACKSON-781
         */
        @JsonProperty("obj")
        NestedLocation nestedLocation;

        public String getName() {
            return nestedLocation.locationName;
        }
    }

    private static class NestedLocation {
        @JsonProperty("name")
        String locationName;
    }
}