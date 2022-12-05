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

package org.jongo.sync;

import com.mongodb.DBObject;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BsonSyncTest extends SyncTestBase {

    private MongoCollection<BsonDocument> collection;

    @Before
    public void setUp() throws Exception {
        collection = jongo.getCollection("friends", BsonDocument.class).withWriteConcern(WriteConcern.ACKNOWLEDGED);
    }

    @After
    public void tearDown() throws Exception {
        collection.drop();
    }

    @Test
    public void canInsert() throws Exception {

        collection.insertOne(q("{name : 'Abby'}"));

        assertThat(collection.countDocuments(q("{name : 'Abby'}"))).isEqualTo(1);
    }

    @Test
    public void canInsertWithParameters() throws Exception {

        collection.insertOne(q("{name : #}", "Abby"));

        assertThat(collection.countDocuments(q("{name : 'Abby'}"))).isEqualTo(1);
    }

    @Test
    public void canFindWithProjectionParams() throws Exception {

        collection.insertOne(q("{name : 'Abby'}"));

        FindIterable<BsonDocument> results = collection.find(q("{name:'Abby'}")).projection(q("{name:#}", 1));

        assertThat(results).isNotEmpty();
        results.map(new Function<BsonDocument, String>() {

            public String apply(BsonDocument bson) {
                BsonDocument document = bson.toBsonDocument(DBObject.class, MongoClient.getDefaultCodecRegistry());
                assertThat(document.containsKey("address")).isFalse();
                assertThat(document.containsKey("name")).isTrue();
                return null;
            }
        });
    }

    @Test
    public void canQueryWithBsonDocument() throws Exception {

        BsonDocument document = new BsonDocument("name", new BsonString("Abby")).append("address", new BsonString("123 Wall Street"));

        collection.insertOne(document);

        Bson result = collection.find(new Document("address", "123 Wall Street")).first();
        BsonDocument bsonDocument = result.toBsonDocument(DBObject.class, MongoClient.getDefaultCodecRegistry());
        assertThat(bsonDocument.toJson()).contains("123 Wall Street");
    }


}
