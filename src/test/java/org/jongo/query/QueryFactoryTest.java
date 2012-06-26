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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jongo.marshall.Marshaller;
import org.jongo.model.People;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;

public class QueryFactoryTest {

    private QueryFactory factory;
    private Marshaller marshaller;

    @Before
    public void setUp() throws Exception {
        marshaller = mock(Marshaller.class);
        factory = new QueryFactory(marshaller);
    }

    @Test
    public void canCreateStaticQuery() throws Exception {

        Query query = factory.createQuery("{value:1}");

        assertThat(query.toString()).isEqualTo("{ \"value\" : 1}");
        verify(marshaller, never()).marshall(any());
    }

    @Test
    public void shouldBindParameterAndCreateQuery() throws Exception {

        when(marshaller.marshall(2)).thenReturn("2");

        Query query = factory.createQuery("{value:#}", 2);

        assertThat(query.toString()).isEqualTo("{ \"value\" : 2}");
        verify(marshaller).marshall(2);
    }

    @Test
    public void shouldBindComplexParameterAndCreateQuery() throws Exception {

        People robert = new People("robert");
        when(marshaller.marshall(robert)).thenReturn("{ \"name\" : \"robert\"}");

        Query query = factory.createQuery("{value:#}", robert);

        assertThat(query.toString()).isEqualTo("{ \"value\" : { \"name\" : \"robert\"}}");
        verify(marshaller).marshall(robert);
    }

    @Test
    public void canCreateEmptyQuery() throws Exception {

        Query query = factory.createEmptyQuery();

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject());
    }
}
