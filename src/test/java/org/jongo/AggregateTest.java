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

import com.mongodb.AggregationOptions;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.AggregationOptions.OutputMode.CURSOR;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregateTest extends JongoTestCase {


    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {

        assumeThatMongoVersionIsGreaterThan("2.6.0");

        collection = createEmptyCollection("articles");
        collection.save(new Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"));
        collection.save(new Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"));
        collection.save(new Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"));
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("articles");
    }

    @Test
    public void shouldAggregateAllDocuments() throws Exception {

        List<Article> articles = collection.aggregate("{$match:{}}").as(Article.class);

        assertThat(articles.isEmpty()).isFalse();
        for (Article article : articles) {
            assertThat(article.title).isIn("Zombie Panic", "Apocalypse Zombie", "World War Z");
        }
    }

    @Test
    public void canAggregateWithMatch() throws Exception {

        List<Article> articles = collection.aggregate("{$match:{tags:'virus'}}").as(Article.class);

        assertThat(articles).hasSize(2);
        for (Article article : articles) {
            assertThat(article.tags).contains("virus");
        }
    }

    @Test
    public void canAggregateParameters() throws Exception {

        List<Article> articles = collection.aggregate("{$match:{tags:#}}", "pandemic").as(Article.class);

        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).title).isEqualTo("World War Z");
    }

    @Test
    public void canAggregateWithManyMatch() throws Exception {

        List<Article> articles = collection.aggregate("{$match:{tags:'virus'}}").and("{$match:{tags:'pandemic'}}").as(Article.class);

        assertThat(articles).hasSize(1);
        Article firstArticle = articles.get(0);
        assertThat(firstArticle.title).isEqualTo("World War Z");
    }

    @Test
    public void canAggregateWithManyOperators() throws Exception {

        List<Article> articles = collection.aggregate("{$match:{tags:'virus'}}").and("{$limit:1}").as(Article.class);

        assertThat(articles.size()).isEqualTo(1);
    }

    @Test
    public void shouldCheckIfCommandHasErrors() throws Exception {

        try {
            collection.aggregate("{$invalid:{}}").as(Article.class);
            fail();
        } catch (Exception e) {
            assertThat(e.getClass().toString()).contains("CommandFailure");
        }
    }

    @Test
    public void shouldAggregateWithOptions() throws Exception {

        List<Article> articles = collection
                .aggregate("{$match:{tags:'virus'}}")
                .options(AggregationOptions.builder().allowDiskUse(true).build())
                .as(Article.class);

        assertThat(articles.size()).isEqualTo(2);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenPassingOptionsWithCursorOutputMode() {

        try {
            Aggregate aggregate = collection.aggregate("{$match:{}}");
            aggregate.options(AggregationOptions.builder().outputMode(CURSOR).build());
            fail();
        } catch (Exception e) {
            assertThat(e instanceof IllegalArgumentException).isTrue();
            assertThat(e.getMessage()).isEqualTo("Cursour output mode is not supported");
        }
    }

    private final static class Article {
        private String title;
        private String author;
        private List<String> tags;

        private Article(String title, String author, String... tags) {
            this.title = title;
            this.author = author;
            this.tags = Arrays.asList(tags);
        }

        private Article() {
        }
    }
}
