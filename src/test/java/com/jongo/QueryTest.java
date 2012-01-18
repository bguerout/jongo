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

import com.mongodb.DBObject;
import org.junit.Test;

import static com.jongo.Query.Builder;
import static com.jongo.Query.query;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryTest {

    @Test
    public void canBuildStaticQuery() throws Exception {

        Query query = query("{'value':1}");

        DBObject dbObject = query.toDBObject();
        assertThat(dbObject.containsField("value")).isTrue();
    }

    @Test
    public void canBuildParameterizedQuery() throws Exception {

        Query query = query("{'value':#}", "1", "2");

        DBObject dbObject = query.toDBObject();
        assertThat(dbObject.get("value")).isEqualTo("1");
    }

    /**
     * TODO check if this test belongs to Query (same test exists in ParameterBinderTest )
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnInvalidParameters() throws Exception {

        ParameterBinder binder = mock(ParameterBinder.class);
        Builder builder = new Builder("{'value':#,'value2':#}", binder).parameters("1");
        when(binder.bind("{'value':#,'value2':#}", "1")).thenThrow(new IllegalArgumentException());

        builder.build();
    }

    @Test
    public void canBuildQueryWithFields() throws Exception {

        Builder builder = new Builder("{'value':#}").parameters("1").fields("{value:1}");

        Query query = builder.build();

        DBObject fields = query.getFields();
        assertThat(fields.get("value")).isEqualTo(1);
    }
}
