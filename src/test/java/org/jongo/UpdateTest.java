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
import org.junit.Rule;
import org.junit.Test;

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class UpdateTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    @Deprecated
    public void canUpdateMultiDeprecated() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));

        /* when */
        collection.update("{name:'John'}", "{$unset:{name:1}}");

        /* then */
        Iterable<People> peoples = collection.find("{name:{$exists:true}}").as(People.class);
        assertThat(peoples).hasSize(0);
    }

    @Test
    public void canUpdateMulti() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}").multi().with("{$unset:{name:1}}");

        /* then */
        Iterable<People> peoples = collection.find("{name:{$exists:true}}").as(People.class);
        assertThat(peoples).hasSize(0);
        assertThat(writeResult.getLastConcern()).isEqualTo(collection.getDBCollection().getWriteConcern());
    }

    @Test
    public void canUpdateMultiWithWriteConcern() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}").multi().concern(WriteConcern.SAFE).with("{$unset:{name:1}}");

        /* then */
        Iterable<People> peoples = collection.find("{name:{$exists:true}}").as(People.class);
        assertThat(peoples).hasSize(0);
        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);

    }

    @Test
    public void canUpsert() throws Exception {

        /* when */
        WriteResult writeResult = collection.update("{}").upsert().with("{$set:{name:'John'}}");

        /* then */
        People john = collection.findOne("{name:'John'}").as(People.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
    }

    @Test
    public void canUpsertWithWriteConcern() throws Exception {

        /* when */
        WriteResult writeResult = collection.update("{}").upsert().concern(WriteConcern.SAFE).with("{$set:{name:'John'}}");

        /* then */
        People john = collection.findOne("{name:'John'}").as(People.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }
}
