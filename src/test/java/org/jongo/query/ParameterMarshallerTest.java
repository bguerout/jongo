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

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ParameterMarshallerTest {

    private ParameterMarshaller marshaller;

    @Before
    public void setUp() throws Exception {
        marshaller = new ParameterMarshaller();
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailWithCharParameter() throws Exception {
        char c = '1';

        marshaller.marshall(c);
    }

    @Test
    public void canMapParameter() throws Exception {

        String query = marshaller.marshall("123");

        assertThat(query).isEqualTo("\"123\"");
    }


    @Test
    public void canMapDate() throws Exception {

        Date epoch = new Date(0);

        String query = marshaller.marshall(epoch);

        assertThat(query).isEqualTo("{ \"$date\" : \"1970-01-01T00:00:00.000Z\"}");
    }

    @Test
    public void canMapList() throws Exception {

        List<String> elements = new ArrayList<String>();
        elements.add("1");
        elements.add("2");

        String query = marshaller.marshall(elements);

        assertThat(query).isEqualTo("[ \"1\" , \"2\"]");
    }

    @Test
    public void canHandleBoolean() throws Exception {

        String query = marshaller.marshall(true);

        assertThat(query).isEqualTo("true");
    }

    @Test
    public void shouldEscapeJsonAsString() throws Exception {

        String query = marshaller.marshall("{injection:true}");

        assertThat(query).isEqualTo("\"{injection:true}\"");
    }


    @Test
    public void canHandleObjectId() throws Exception {

        String query = marshaller.marshall(new ObjectId("47cc67093475061e3d95369d"));

        assertThat(query).isEqualTo("{ \"$oid\" : \"47cc67093475061e3d95369d\"}");
    }


}
