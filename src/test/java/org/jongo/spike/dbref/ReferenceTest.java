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

package org.jongo.spike.dbref;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import org.codehaus.jackson.map.ObjectMapper;
import org.jongo.spike.dbref.jackson.Reference;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ReferenceTest {

    private DBRef dbRef;
    private ObjectMapper mapper;
    private Reference reference;

    @Before
    public void setUp() throws Exception {
        dbRef = mock(DBRef.class);
        mapper = mock(ObjectMapper.class);
        reference = new Reference(dbRef, mapper);
    }

    @Test
    public void shouldFetchAndMarshallResult() throws Exception {

        when(dbRef.fetch()).thenReturn(new BasicDBObject());

        reference.as(ReferenceTest.class);

        verify(dbRef).fetch();
        verify(mapper).readValue("{ }", ReferenceTest.class);
    }
}
