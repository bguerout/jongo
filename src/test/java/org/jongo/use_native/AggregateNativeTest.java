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

import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import org.jongo.model.Article;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregateNativeTest extends NativeTestBase {

    private MongoCollection<Article> collection;

    @Before
    public void setUp() throws Exception {
        collection = jongo.getCollection("articles", Article.class);
        collection.insertOne(new Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"));
        collection.insertOne(new Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"));
        collection.insertOne(new Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"));
    }

    @After
    public void tearDown() throws Exception {
        this.collection.drop();
    }

    @Test
    public void canAggregate() throws Exception {

        List<Bson> pipeline = asList(q("{$match:{tags:'virus'}}"));
        Iterable<Article> articles = collection.aggregate(pipeline);

        assertThat(articles.iterator().hasNext()).isTrue();
        int size = 0;
        for (Article article : articles) {
            assertThat(article.getTags()).contains("virus");
            size++;
        }
        assertThat(size).isEqualTo(2);
    }
}
