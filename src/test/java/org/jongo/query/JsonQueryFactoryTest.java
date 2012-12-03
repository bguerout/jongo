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
import com.mongodb.QueryBuilder;
import org.jongo.marshall.jackson.ConfigurationHelper;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.util.ErrorObject;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class JsonQueryFactoryTest {

    private QueryFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new JsonQueryFactory(new JacksonEngine(ConfigurationHelper.mapping()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidParameter() throws Exception {

        factory.createQuery("{id:#}", new ErrorObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {

        factory.createQuery("{id:#,id2:#}", "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTooManyParameters() throws Exception {

        factory.createQuery("{id:#}", 123, 456);
    }

    @Test
    public void shouldBindOneParameter() throws Exception {

        Query query = factory.createQuery("{id:#}", 123);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", 123));
    }

    @Test
    public void shouldBindManyParameters() throws Exception {

        Query query = factory.createQuery("{id:#, test:#}", 123, 456);

        assertThat(query.toDBObject()).isEqualTo(QueryBuilder.start("id").is(123).and("test").is(456).get());
    }

    @Test
    public void shouldBindNullParameter() throws Exception {

        Query query = factory.createQuery("{id:#}", null);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", null));
    }

    @Test
    public void shouldBindParameterWithCustomToken() throws Exception {

        QueryFactory factoryWithToken = new JsonQueryFactory(new JacksonEngine(ConfigurationHelper.mapping()), "@");

        Query query = factoryWithToken.createQuery("{id:@}", 123);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", 123));
    }


}
