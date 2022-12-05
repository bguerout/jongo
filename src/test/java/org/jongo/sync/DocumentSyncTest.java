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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentSyncTest extends SyncTestBase {

    private MongoCollection<Document> collection;

    @Before
    public void setUp() throws Exception {
        collection = jongo.getCollection("friends", Document.class);
    }

    @After
    public void tearDown() throws Exception {
        collection.drop();
    }

    @Test
    public void canUseDocumentBuilderFromDriver() throws Exception {

        Document doc = new Document().append("name", "Abby");

        collection.insertOne(doc);

        assertThat(collection.countDocuments(doc)).isEqualTo(1);
    }

    @Test
    public void canUseFilterBuilderFromDriver() throws Exception {

        Document doc = new Document().append("name", "Abby");

        collection.insertOne(doc);

        Document result = collection.find(Filters.regex("name", Pattern.compile("^A"))).first();
        assertThat(result.get("name")).isEqualTo("Abby");
    }


}
