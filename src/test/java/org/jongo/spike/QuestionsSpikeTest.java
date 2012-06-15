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

package org.jongo.spike;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class QuestionsSpikeTest extends JongoTestCase {

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
    // http://stackoverflow.com/questions/10444038/mongo-db-query-in-java/10445169#10445169
    public void complexQueryWithDriverAndJongo() throws Exception {

        List<String> keys = new ArrayList<String>();
        collection.findOne("{$or:[{key1: {$in:[764]}},{key2:{$in:[#]}}, {$and:[{key3:3},{key4:67}]}]}", keys).as(People.class);

        DBObject query = QueryBuilder
                .start()
                .or(QueryBuilder.start("key1").in(764).get(), QueryBuilder.start("key2").in(keys).get(),
                        QueryBuilder.start().and("key3").is(3).and("key4").is(64).get()).get();

        getDatabase().getCollection("users").find(query);
    }

    @Test
    // https://groups.google.com/forum/?hl=fr&fromgroups#!topic/jongo-user/ga3n5_ybYm4
    public void pushANonBSONObject() throws Exception {
        Peoples peoples = new Peoples();
        peoples.add(new People("john"));
        peoples.add(new People("peter"));
        collection.save(peoples);

        String robert = new JacksonProcessor().marshall(new People("Robert"));
        collection.update("{}").with("{$push:{peoples:" + robert + "}}");

        assertThat(collection.count("{ peoples.name : 'Robert'}")).isEqualTo(1);
    }

    private static class Peoples {
        private List<People> peoples = new ArrayList<People>();

        public void add(People buddy) {
            peoples.add(buddy);
        }
    }
}
