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

package org.jongo.spike.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.DBObject;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.model.Coordinate;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonProjectionTest {

    private JacksonProjection projection;

    @Before
    public void setUp() throws Exception {
        Mapping mapping = new Mapping.Builder().build();
        projection = new JacksonProjection(mapping.getObjectMapper());
    }

    @Test
    public void canCreateQueryForPOJO() throws Exception {

        DBObject fields = projection.getProjectionQuery(Friend.class).toDBObject();

        assertThat(sanitize(fields.toString()))
                .isEqualTo("{\"name\":1,\"address\":1,\"coordinate\":{\"lat\":1,\"lng\":1},\"gender\":1}");
    }

    @Test
    public void canCreateQueryForPOJOWithSuperType() throws Exception {

        DBObject fields = projection.getProjectionQuery(Fox.class).toDBObject();

        assertThat(sanitize(fields.toString())).isEqualTo("{\"name\":1,\"color\":1,\"gender\":1}");
    }

    @Test
    public void shouldHonorJacksonAnnotations() throws Exception {

        DBObject fields = projection.getProjectionQuery(HiddenCoordinate.class).toDBObject();

        assertThat(sanitize(fields.toString())).isEqualTo("{\"lng\":1}");
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailWhenUnableToCreateFieldsDBO() throws Exception {

        projection.getProjectionQuery(Object.class);
    }

    private String sanitize(String value) {
        return value.replaceAll(" ", "");
    }

    private static class HiddenCoordinate extends Coordinate {


        @JsonIgnore
        public int lat;

        private HiddenCoordinate(int lat, int lng) {
            super(lat, lng);
        }
    }
}
