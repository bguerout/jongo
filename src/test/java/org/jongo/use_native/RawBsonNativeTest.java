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

package org.jongo.use_native;

import com.mongodb.DBObject;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RawBsonNativeTest extends NativeTestBase {

    private MongoCollection<Bson> rawCollection;

    @Before
    public void setUp() throws Exception {
        rawCollection = createNativeCollection("friends").withWriteConcern(WriteConcern.ACKNOWLEDGED);
    }

    @Test
    public void canInsert() throws Exception {

        rawCollection.insertOne(q("{name : 'Abby'}"));

        assertThat(rawCollection.count(q("{name : 'Abby'}"))).isEqualTo(1);
    }

    @Test
    public void canInsertWithParameters() throws Exception {

        rawCollection.insertOne(q("{name : #}", "Abby"));

        assertThat(rawCollection.count(q("{name : 'Abby'}"))).isEqualTo(1);
    }

    @Test
    public void canFindWithProjectionParams() throws Exception {

        rawCollection.insertOne(q("{name : 'Abby'}"));

        FindIterable<Bson> results = rawCollection.find(q("{name:'Abby'}")).projection(q("{name:#}", 1));

        assertThat(results).isNotEmpty();
        results.map(new Function<Bson, String>() {

            public String apply(Bson bson) {
                BsonDocument document = bson.toBsonDocument(DBObject.class, MongoClient.getDefaultCodecRegistry());
                assertThat(document.containsKey("address")).isFalse();
                assertThat(document.containsKey("name")).isTrue();
                return null;
            }
        });
    }


}