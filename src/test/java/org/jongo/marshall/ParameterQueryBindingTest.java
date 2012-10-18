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

import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import org.jongo.MongoCollection;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.model.Gender;
import org.jongo.util.DBObjectResultMapper;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ParameterQueryBindingTest extends JongoTestCase {

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
    public void shouldBindOneParameter() throws Exception {

        long nb = collection.count("{name:#}", "robert");

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindManyParameters() throws Exception {

        long nb = collection.count("{coordinate.lat:#, coordinate.lng:#}", 2, 3);

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindListOfPrimitive() throws Exception {

        List<String> strings = Lists.newArrayList("1", "2");

        long nb = collection.count("{coordinate.lat:{$in:#}}", strings);

        assertThat(nb).isEqualTo(1);
    }

    @Test
    public void shouldBindEnumParameter() throws Exception {

        Friend friend = new Friend("John", new Coordinate(2, 31));
        friend.setGender(Gender.FEMALE);
        collection.save(friend);

        Iterator<Friend> results = collection.find("{'gender':#}", Gender.FEMALE).as(Friend.class).iterator();

        assertThat(results.next().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(results.hasNext()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidParameter() throws Exception {

        collection.findOne("{id:#}", new ErrorObject()).as(Friend.class);
    }

    @Test
    public void shouldFindWithADynamicFieldName() throws Exception {

        /* given */
        collection.insert("{name:{1:'John'}}");

        /* when */
        DBObject result = collection.findOne("{name.#:#}", 1, "John").map(new DBObjectResultMapper());

        /* then */
        assertThat(result).isNotNull();
        assertThat(result.get("name")).isInstanceOf(DBObject.class);
        assertThat(((DBObject) result.get("name")).get("1")).isEqualTo("John");
    }

}
