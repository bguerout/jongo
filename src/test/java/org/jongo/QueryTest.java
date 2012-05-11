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

import com.mongodb.DBObject;
import org.jongo.marshall.MongoDriverMarshaller;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.model.Coordinate;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class QueryTest {

    private ParameterBinder binder;

    @Before
    public void setUp() throws Exception {
        binder = new ParameterBinder(new MongoDriverMarshaller());
    }

    @Test
    public void canBuildStaticQuery() throws Exception {

        Query query = new Query("{'value':1}", binder);

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.containsField("value")).isTrue();
    }

    @Test
    public void canBuildParameterizedQueryWithBsonPrimitive() throws Exception {

        Query query = new Query("{'value':#}", binder, "1");

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("value")).isEqualTo("1");
    }

    @Test
    public void canBuildParameterizedQueryWithComplexType() throws Exception {

        ParameterBinder jacksonBinder = new ParameterBinder(new JacksonProcessor());
        Query query = new Query("{'value':#}", jacksonBinder, new Coordinate(1, 1));

        DBObject dbObject = query.toDBObject();

        Object value = dbObject.get("value");
        assertThat(value).isInstanceOf(DBObject.class);
        assertThat(((DBObject) value).get("lat")).isEqualTo(1);
        assertThat(((DBObject) value).get("lng")).isEqualTo(1);
    }
}
