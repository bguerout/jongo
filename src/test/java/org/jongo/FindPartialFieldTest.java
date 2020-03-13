/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FindPartialFieldTest extends JongoTestBase {

    private MongoCollection collection;
    private Friend friend;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
        friend = new Friend("John", "22 Wall Street Avenue");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        collection.find("{name:'John'}").projection("{name:1}").map(new AssertionResultHandler());
    }

    @Test
    public void canFindWithProjectionParams() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        collection.find("{name:'John'}").projection("{name:#}", 1).map(new AssertionResultHandler());
    }

    @Test
    public void canFindWithComplexProjection() throws Exception {
        /* given */
        collection.insert("{subElements: [{ name: \"foo\"},{ name: \"bar\"}]}");

        /* when */
        Iterator<Map> maps = collection.find().projection("{ subElements: {$elemMatch: {name: #} } }", "bar").as(Map.class);

        Map map = maps.next();
        assertThat(map.get("subElements")).isNotNull();
        List subElements = ((List) map.get("subElements"));
        assertThat(subElements).hasSize(1);
    }

    @Test
    public void canFindOne() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        Boolean result = collection.findOne("{name:'John'}").projection("{name:1}").map(new AssertionResultHandler());

        assertThat(result).isTrue();
    }

    @Test
    public void canFindOneWithProjectionParams() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        Boolean result = collection.findOne("{name:'John'}").projection("{name:#}", 1).map(new AssertionResultHandler());

        assertThat(result).isTrue();
    }

    @Test
    public void canFindOneWithComplexProjection() throws Exception {
        /* given */
        collection.insert("{subElements: [{ name: \"foo\"},{ name: \"bar\"}]}");

        /* when */
        Map map = collection.findOne().projection("{ subElements: {$elemMatch: {name: #} } }", "bar").as(Map.class);

        assertThat(map).isNotNull();
        assertThat(map.get("subElements")).isNotNull();
        List subElements = ((List) map.get("subElements"));
        assertThat(subElements).hasSize(1);
    }

    @Test
    public void shouldIgnoreNullProjection() throws Exception {
        /* given */
        collection.save(friend);

        /* when */
        Friend result = collection.findOne("{name:'John'}").projection(null).as(Friend.class);

        assertThat(friend.getName()).isEqualTo("John");
        assertThat(friend.getAddress()).isEqualTo("22 Wall Street Avenue");
    }

    @Test
    public void canFindOneWithProjectionParamsWithDot() throws Exception {
        collection.save(new Friend("John", "23 Wall Street Av.", new Coordinate(1, 1)));

        Map map = collection.findOne("{name:'John'}").projection("{coordinate.lat:#}", 1).as(Map.class);

        assertThat(((Map) map.get("coordinate")).get("lng")).isNull();
        assertThat(((Map) map.get("coordinate")).get("lat")).isNotNull();
    }


    private static class AssertionResultHandler implements ResultHandler<Boolean> {
        public Boolean map(DBObject result) {
            assertThat(result.containsField("address")).isFalse();
            assertThat(result.containsField("name")).isTrue();
            return true;
        }
    }
}
