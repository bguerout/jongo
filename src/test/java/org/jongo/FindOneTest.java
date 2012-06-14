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

import org.jongo.model.People;
import org.jongo.util.IdResultMapper;
import org.junit.Rule;
import org.junit.Test;

public class FindOneTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canFindOne() throws Exception {
        /* given */
        String id = collection.save(new People("John", "22 Wall Street Avenue"));

        /* when */
        People people = collection.findOne("{name:'John'}").as(People.class);

        /* then */
        assertThat(people.getId()).isEqualTo(id);
    }

    @Test
    public void canFindOneWithEmptyQuery() throws Exception {
        /* given */
        String id = collection.save(new People("John", "22 Wall Street Avenue"));

        /* when */
        People people = collection.findOne("{}").as(People.class);

        /* then */
        assertThat(people.getId()).isEqualTo(id);
    }

    @Test
    public void whenNoResultShouldReturnNull() throws Exception {
        assertThat(collection.findOne("{_id:'invalid-id'}").as(Object.class)).isNull();
        assertThat(collection.findOne("{_id:'invalid-id'}").map(new IdResultMapper())).isNull();
        assertThat(collection.find("{_id:'invalid-id'}").as(Object.class)).hasSize(0);
    }

}
