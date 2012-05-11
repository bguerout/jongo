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

import org.bson.types.ObjectId;
import org.jongo.marshall.MongoDriverMarshaller;
import org.jongo.marshall.Marshaller;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ParameterBinderTest {

    private ParameterBinder binder;

    @Before
    public void setUp() throws Exception {
        binder = new ParameterBinder(new MongoDriverMarshaller());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithCharParameter() throws Exception {
        char c = '1';

        binder.bind("{id:#}", c);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {

        binder.bind("{id:#,id2:#}", "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotTooManyParameters() throws Exception {

        binder.bind("{id:#}", 123, 456);
    }

    @Test
    public void canMapParameter() throws Exception {

        String query = binder.bind("{id:#}", "123");

        assertThat(query).isEqualTo("{id:\"123\"}");
    }

    @Test
    public void canMapParameterWithCustomToken() throws Exception {

        String query = new ParameterBinder(new MongoDriverMarshaller(), "@").bind("{id:@}", "123");

        assertThat(query).isEqualTo("{id:\"123\"}");
    }

    @Test
    public void canMapParameters() throws Exception {

        String query = binder.bind("{id:#, test:#}", "123", "456");

        assertThat(query).isEqualTo("{id:\"123\", test:\"456\"}");
    }

    @Test
    public void canMapDate() throws Exception {

        Date epoch = new Date(0);

        String query = binder.bind("{mydate:#}", epoch);

        assertThat(query).isEqualTo("{mydate:{ \"$date\" : \"1970-01-01T00:00:00.000Z\"}}");
    }

    @Test
    public void canMapList() throws Exception {

        List<String> elements = new ArrayList<String>();
        elements.add("1");
        elements.add("2");

        String query = binder.bind("{$in:#}", elements);

        assertThat(query).isEqualTo("{$in:[ \"1\" , \"2\"]}");
    }

    @Test
    public void canHandleBoolean() throws Exception {

        String query = binder.bind("{id:#}", true);

        assertThat(query).isEqualTo("{id:true}");
    }

    @Test
    public void shouldEscapeJsonAsString() throws Exception {

        String query = binder.bind("{value:#}", "{injection:true}");

        assertThat(query).isEqualTo("{value:\"{injection:true}\"}");
    }


    @Test
    public void canHandleObjectId() throws Exception {

        String query = binder.bind("{_id:#}", new ObjectId("47cc67093475061e3d95369d"));

        assertThat(query).isEqualTo("{_id:{ \"$oid\" : \"47cc67093475061e3d95369d\"}}");
    }


    @Test
    public void shouldDelegateMarshalling() throws Exception {

        Marshaller marshaller = mock(Marshaller.class);
        binder = new ParameterBinder(marshaller);

        when(marshaller.marshall(any())).thenReturn("{}");

        Object param = new Object();
        binder.bind("{coordinate:#}", param);

        verify(marshaller).marshall(param);
    }


}


