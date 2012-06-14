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

import org.jongo.model.People;
import org.junit.Rule;
import org.junit.Test;

public class CountTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canCount() throws Exception {
        /* given */
        collection.save(newPeople());
        collection.save(newPeople());

        /* then */
        assertThat(collection.count()).isEqualTo(2);
    }

    @Test
    public void canCountWithQuery() throws Exception {
        /* given */
        collection.save(newPeople());
        collection.save(newPeople());

        /* then */
        assertThat(collection.count("{name:{$exists:true}}")).isEqualTo(2);
    }

    @Test
    public void canCountWithParameters() throws Exception {
        /* given */
        collection.save(newPeople());
        collection.save(new People("Peter", "22 Wall Street Avenue"));

        /* then */
        assertThat(collection.count("{name:#}", "Peter")).isEqualTo(1);
    }
}
