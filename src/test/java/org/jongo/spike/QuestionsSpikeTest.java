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
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.BsonEngine;
import org.jongo.model.Friend;
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
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    // http://stackoverflow.com/questions/10444038/mongo-db-query-in-java/10445169#10445169
    public void complexQueryWithDriverAndJongo() throws Exception {

        List<String> keys = new ArrayList<String>();
        collection.findOne("{$or:[{key1: {$in:[764]}},{key2:{$in:[#]}}, {$and:[{key3:3},{key4:67}]}]}", keys).as(Friend.class);

        DBObject query = QueryBuilder
                .start()
                .or(QueryBuilder.start("key1").in(764).get(), QueryBuilder.start("key2").in(keys).get(),
                        QueryBuilder.start().and("key3").is(3).and("key4").is(64).get()).get();

        getDatabase().getCollection("friends").find(query);
    }

    @Test
    public void testparse() throws Exception {
        ObjectId id = ObjectId.get();
        Object dbo = JSON.parse("{_id:{$oid:'" + id + "'}}");

        assertThat(dbo).isInstanceOf(DBObject.class);
    }

    @Test
    // https://groups.google.com/forum/?hl=fr&fromgroups#!topic/jongo-user/ga3n5_ybYm4
    public void pushANonBSONObject() throws Exception {
        Friends friends = new Friends();
        friends.add(new Friend("john"));
        friends.add(new Friend("peter"));
        collection.save(friends);

        DBObject robert = new BsonEngine().marshall(new Friend("Robert")).toDBObject();
        collection.update("{}").with("{$push:{friends:" + robert.toString() + "}}");

        assertThat(collection.count("{ 'friends.name' : 'Robert'}")).isEqualTo(1);
    }

    private static class Friends {
        private List<Friend> friends = new ArrayList<Friend>();

        public void add(Friend buddy) {
            friends.add(buddy);
        }
    }
}
