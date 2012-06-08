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
import org.jongo.query.QueryFactory;
import org.jongo.query.ParameterizedQuery;
import org.jongo.query.Query;
import org.jongo.query.StaticQuery;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class QueryFactoryTest {

    private QueryFactory queryFactory;

    @Before
    public void setUp() throws Exception {
        queryFactory = new QueryFactory();
    }

    @Test
    public void shouldCreateBindableQuery() throws Exception {

        Query query = queryFactory.createQuery("{value:#}", 1);

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.get("value")).isEqualTo(1);
        assertThat(query).isInstanceOf(ParameterizedQuery.class);
    }

    @Test
    public void shouldCreateStaticQuery() throws Exception {

        Query query = queryFactory.createQuery("{value:1}");

        assertThat(query).isInstanceOf(StaticQuery.class);
    }

    @Test
    public void shouldCreateAnEmptyQuery() throws Exception {

        Query query = queryFactory.createEmptyQuery();

        assertThat(query).isInstanceOf(StaticQuery.class);
        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject());
    }
}
