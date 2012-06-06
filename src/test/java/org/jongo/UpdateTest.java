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

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class UpdateTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canUpdateMulti() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}", "{$unset:{name:1}}");

        /* then */
        Iterator<People> peoples = collection.find("{name:{$exists:true}}").as(People.class).iterator();
        assertThat(peoples).hasSize(0);
        assertThat(writeResult.getLastConcern()).isEqualTo(collection.getDBCollection().getWriteConcern());
    }

    @Test
    public void canUpdateMultiWithWriteConcern() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}", "{$unset:{name:1}}", WriteConcern.SAFE);

        /* then */
        Iterator<People> peoples = collection.find("{name:{$exists:true}}").as(People.class).iterator();
        assertThat(peoples).hasSize(0);
        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);

    }

    @Test
    public void canUpsert() throws Exception {

        /* when */
        WriteResult writeResult = collection.upsert("{}", "{$set:{name:'John'}}");

        /* then */
        People john = collection.findOne("{name:'John'}").as(People.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
    }

    @Test
    public void canUpsertWithWriteConcern() throws Exception {

        WriteConcern writeConcern = spy(WriteConcern.SAFE);

        /* when */
        WriteResult writeResult = collection.upsert("{}", "{$set:{name:'John'}}", writeConcern);

        /* then */
        People john = collection.findOne("{name:'John'}").as(People.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
        verify(writeConcern).callGetLastError();
    }
}
