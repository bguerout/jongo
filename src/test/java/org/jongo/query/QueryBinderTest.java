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

import org.jongo.marshall.QueryMarshaller;
import org.jongo.marshall.jackson.JacksonQueryMarshaller;
import org.jongo.marshall.jackson.JsonEngine;
import org.jongo.util.ErrorObject;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class QueryBinderTest {

    private QueryBinder binder;

    @Before
    public void setUp() throws Exception {
        QueryMarshaller queryMarshaller = new JacksonQueryMarshaller(new JsonEngine());
        binder = new QueryBinder(queryMarshaller);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidParameter() throws Exception {

        binder.bind("{id:#}", new ErrorObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {

        binder.bind("{id:#,id2:#}", "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTooManyParameters() throws Exception {

        binder.bind("{id:#}", 123, 456);
    }

    @Test
    public void shouldBindOneParameter() throws Exception {

        String query = binder.bind("{id:#}", 123);

        assertThat(query).isEqualTo("{id:123}");
    }

    @Test
    public void shouldBindManyParameters() throws Exception {

        String query = binder.bind("{id:#, test:#}", 123, 456);

        assertThat(query).isEqualTo("{id:123, test:456}");
    }

    @Test
    public void shouldBindParameterWithCustomToken() throws Exception {

        QueryMarshaller queryMarshaller = new JacksonQueryMarshaller(new JsonEngine());
        QueryBinder binderWithToken = new QueryBinder(queryMarshaller, "@");

        String query = binderWithToken.bind("{id:@}", 123);

        assertThat(query).isEqualTo("{id:123}");
    }

}


