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
import org.jongo.ResultHandler;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.model.Friend;
import org.jongo.util.JSONResultHandler;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    // https://groups.google.com/forum/?hl=fr&fromgroups#!topic/jongo-user/ga3n5_ybYm4
    public void pushANonBSONObject() throws Exception {
        Party party = new Party();
        party.with(new Friend("john"));
        party.with(new Friend("peter"));
        collection.save(party);

        DBObject robert = new JacksonEngine(Mapping.defaultMapping()).marshall(new Friend("Robert")).toDBObject();
        collection.update("{}").with("{$push:{friends:" + robert.toString() + "}}");

        assertThat(collection.count("{ 'friends.name' : 'Robert'}")).isEqualTo(1);
    }

    @Test
    // https://groups.google.com/forum/?fromgroups=#!topic/jongo-user/UVOEmP-ql_k
    public void canHandleElemMatchOperator() throws Exception {

        assumeThatMongoVersionIsGreaterThan("2.1.1");

        collection.insert("{version : 1, days:[{name:'monday'},{name:'sunday'}]}");
        collection.insert("{version : 2, days:[{name:'wednesday'}]}");

        String monday = collection.findOne("{version:1}").projection("{days:{$elemMatch:{name: 'monday'}}}").map(new JSONResultHandler());

        assertThat(monday).contains("\"days\" : [ { \"name\" : \"monday\"}]");
    }

    @Test
    //https://groups.google.com/forum/?fromgroups#!topic/jongo-user/Nu4J1tK0kAM
    public void canSelectOnlyAField() throws Exception {

        final Unmarshaller unmarshaller = getMapper().getUnmarshaller();
        Party party = new Party();
        party.with(new Friend("John"));
        party.with(new Friend("Peter"));
        party.with(new Friend("Robert"));
        collection.save(party);

        Friend friend = collection.findOne("{friends.name:'Peter'}").projection("{friends.$:1}").map(new ResultHandler<Friend>() {
            public Friend map(DBObject dbo) {
                Party result = unmarshaller.unmarshall((BsonDocument) dbo, Party.class);
                return result.friends.get(0);
            }
        });

        assertThat(friend.getName()).isEqualTo("Peter");
    }

    @Test
    //https://groups.google.com/forum/?fromgroups#!topic/jongo-user/p9CEKnkKX9Q
    public void canUpdateIntoAnArray() throws Exception {

        collection.insert("{friends:[{name:'Robert'},{name:'Peter'}]}");

        collection.update("{ 'friends.name' : 'Peter' }").with("{ $set : { 'friends.$' : #} }", new Friend("John"));

        Party party = collection.findOne().as(Party.class);

        assertThat(party.friends).extracting("name").containsExactly("Robert", "John");
    }

    private static class Party {
        private List<Friend> friends = new ArrayList<Friend>();

        public void with(Friend friend) {
            friends.add(friend);
        }
    }
}
