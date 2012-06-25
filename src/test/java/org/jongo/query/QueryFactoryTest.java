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

package org.jongo.query;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jongo.model.People;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class QueryFactoryTest {

    private QueryFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new QueryFactory();
    }

    @Test
    public void canCreateStaticQuery() throws Exception {

        Query query = factory.createQuery("{'value':1}");

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.containsField("value")).isTrue();
        assertThat(dbObject.get("value")).isEqualTo(1);
    }

    @Test
    public void canCreateParameterizedQuery() throws Exception {

        Query query = factory.createQuery("{'value':#}", 2);

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.containsField("value")).isTrue();
        assertThat(dbObject.get("value")).isEqualTo(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenParameterCannotBeMarshalled() throws Exception {

        factory.createQuery("{value:#}", new People("robert"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTooManyParameters() throws Exception {

        factory.createQuery("{value:#}", 1, 2, 3);
    }

    @Test
    public void canCreateEmptyQuery() throws Exception {

        Query query = factory.createEmptyQuery();

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject());
    }
}
