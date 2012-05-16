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
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class QueryTest {

    private ParameterBinder binder;

    @Before
    public void setUp() throws Exception {
        binder = mock(ParameterBinder.class);
    }

    @Test
    public void shouldConvertToDBObject() throws Exception {

        Query query = new Query(binder, "{'value':1}");

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.containsField("value")).isTrue();
        assertThat(dbObject.get("value")).isEqualTo(1);
        verify(binder, never()).bind(anyString(), anyVararg());
    }

    @Test
    public void shouldBindParamsAndConvertToDBObject() throws Exception {

        Query query = new Query(binder, "{'value':#}", "2");
        when(binder.bind("{'value':#}", "2")).thenReturn("{'value':2}");

        DBObject dbObject = query.toDBObject();

        verify(binder).bind("{'value':#}", "2");
        assertThat(dbObject.containsField("value")).isTrue();
        assertThat(dbObject.get("value")).isEqualTo(2);
    }

}
