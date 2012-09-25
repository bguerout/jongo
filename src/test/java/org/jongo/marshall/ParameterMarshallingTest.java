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

package org.jongo.marshall;

import org.jongo.MongoCollection;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.model.Gender;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class ParameterMarshallingTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("marshalling");
        collection.save(new Friend("robert", new Coordinate(2, 3)));
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("marshalling");
    }

    @Test
    public void shouldBindBSONPrimitiveParameter() throws Exception {

        long nb = collection.count("{name:#}", "robert");

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindComplexParameter() throws Exception {

        collection.update("{name:'robert'}").with("{friend:#}", new Friend("john"));

        long nb = collection.count("{friend.name:#}", "john");
        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindEnumParameter() throws Exception {
        /* given */
        Friend friend = new Friend("John", new Coordinate(2, 31));
        friend.setGender(Gender.FEMALE);
        collection.save(friend);

        /* when */
        Iterator<Friend> results = collection.find("{'gender':#}", Gender.FEMALE).as(Friend.class).iterator();

        /* then */
        assertThat(results.next().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(results.hasNext()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {

        collection.findOne("{id:#,id2:#}", "123").as(Friend.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotTooManyParameters() throws Exception {

        collection.findOne("{id:#}", 123, 456).as(Friend.class);
    }


}
