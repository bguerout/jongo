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

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.JongoTest.collection;
import static org.jongo.JongoTest.newPeople;

import org.junit.Rule;
import org.junit.Test;

import com.mongodb.DBObject;

public class FindPartialFieldTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canFind() throws Exception {
        /* given */
        collection.save(newPeople());

        /* when */
        collection.find("{name:'John'}").fields("{name:1}").map(new AssertionResultMapper());
    }

    @Test
    public void canFindOne() throws Exception {
        /* given */
        collection.save(newPeople());

        /* when */
        Boolean result = collection.findOne("{name:'John'}").fields("{name:1}").map(new AssertionResultMapper());

        assertThat(result).isTrue();
    }

    private static class AssertionResultMapper implements ResultMapper<Boolean> {
        public Boolean map(DBObject dbObject) {
            assertThat(dbObject.containsField("address")).isFalse();
            assertThat(dbObject.containsField("name")).isTrue();
            return true;
        }
    }
}
