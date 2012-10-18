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

import com.mongodb.DBObject;
import org.jongo.marshall.jackson.JacksonQueryMarshaller;
import org.jongo.marshall.jackson.JsonEngine;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

public class QueryFactoryTest {

    private QueryFactory factory;

    @Before
    public void setUp() throws Exception {
        JacksonQueryMarshaller marshaller = new JacksonQueryMarshaller(new JsonEngine());
        factory = new QueryFactory(marshaller);
    }

    @Test
    public void canCreateQuery() throws Exception {

        Query query = factory.createQuery("{value:1}");

        assertThat(query.toString()).isEqualTo("{ \"value\" : 1}");
    }

    @Test
    public void shouldBindManyParameterAndCreateQuery() throws Exception {

        Query query = factory.createQuery("{value:#}", 123);

        assertThat(query.toString()).isEqualTo("{ \"value\" : 123}");
    }

    @Test
    public void shouldThrowExceptionOnInvalidQuery() throws Exception {

        try {
            factory.createQuery("{invalid}");
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("{invalid}");
        }
    }

}
