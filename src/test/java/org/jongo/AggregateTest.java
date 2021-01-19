/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

import com.google.common.collect.Lists;
import com.mongodb.AggregationOptions;
import com.mongodb.MongoCommandException;
import org.jongo.Aggregate.ResultsIterator;
import org.jongo.model.Article;
import org.jongo.model.Friend;
import org.jongo.model.TypeWithNested;
import org.jongo.model.TypeWithNested.NestedDocument;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AggregateTest extends JongoTestBase {


    private MongoCollection collection;
    private MongoCollection friendCollection;
    private MongoCollection nestedCollection;
    private ResultsIterator<Article> articles;

    @Before
    public void setUp() throws Exception {
        this.articles = null;
        assumeThatMongoVersionIsGreaterThan("2.6");

        collection = createEmptyCollection("articles");
        collection.save(new Article("Zombie Panic", "Kirsty Mckay", "horror", "virus"));
        collection.save(new Article("Apocalypse Zombie", "Maberry Jonathan", "horror", "dead"));
        collection.save(new Article("World War Z", "Max Brooks", "horror", "virus", "pandemic"));

        friendCollection = createEmptyCollection("friends");
        friendCollection.save(new Friend("William"));
        friendCollection.save(new Friend("John"));
        friendCollection.save(new Friend("Richard"));

        nestedCollection = createEmptyCollection("nested");
        nestedCollection.save(new TypeWithNested()
                .addNested(new NestedDocument().withName("name1").withValue("value1"))
                .addNested(new NestedDocument().withName("name2").withValue("value2")));
        nestedCollection.save(new TypeWithNested()
                .addNested(new NestedDocument().withName("name3").withValue("value3"))
                .addNested(new NestedDocument().withName("name4").withValue("value4")));
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("external_type");
        dropCollection("friends");
        dropCollection("articles");

        if (this.articles != null) {
            this.articles.close();
        }
    }

    @Test
    public void canAggregate() {

        articles = collection.aggregate("{$match:{}}").as(Article.class);

        assertThat(articles.iterator().hasNext()).isTrue();
        for (Article article : articles) {
            assertThat(article.getTitle()).isIn("Zombie Panic", "Apocalypse Zombie", "World War Z");
        }
    }

    @Test
    public void canAggregateWithDefaultOptions() {
        AggregationOptions options = AggregationOptions.builder().build();
        articles = collection.aggregate("{$match:{}}").options(options).as(Article.class);

        assertThat(articles.iterator().hasNext()).isTrue();
        for (Article article : articles) {
            assertThat(article.getTitle()).isIn("Zombie Panic", "Apocalypse Zombie", "World War Z");
        }

        assertThat(articles.isCursor()).isTrue();
    }

    @Test
    public void canAggregateWithOptions() {

        AggregationOptions options = spy(AggregationOptions.builder().allowDiskUse(true).build());

        articles = collection.aggregate("{$match:{}}").options(options).as(Article.class);

        assertThat(articles.iterator().hasNext()).isTrue();
        for (Article article : articles) {
            assertThat(article.getTitle()).isIn("Zombie Panic", "Apocalypse Zombie", "World War Z");
        }
        verify(options, atLeastOnce()).getAllowDiskUse();
        verify(options, atLeastOnce()).getMaxTime(any(TimeUnit.class));
        verify(options, atLeastOnce()).getBatchSize();
        assertThat(articles.isCursor()).isTrue();
    }

    @Test
    public void canAggregateWithMultipleDocuments() {

        articles = collection.aggregate("{$match:{tags:'virus'}}").as(Article.class);

        assertThat(articles.iterator().hasNext()).isTrue();
        int size = 0;
        for (Article article : articles) {
            assertThat(article.getTags()).contains("virus");
            size++;
        }
        assertThat(size).isEqualTo(2);
    }

    @Test
    public void canAggregateParameters() {

        ResultsIterator<Article> articles = collection.aggregate("{$match:{tags:#}}", "pandemic").as(Article.class);

        assertThat(articles.next().getTitle()).isEqualTo("World War Z");
        assertThat(articles.hasNext()).isFalse();
    }

    @Test
    public void canAggregateWithManyMatch() {

        articles = collection.aggregate("{$match:{tags:'virus'}}").and("{$match:{tags:'pandemic'}}").as(Article.class);

        Article firstArticle = articles.next();
        assertThat(firstArticle.getTitle()).isEqualTo("World War Z");
        assertThat(articles.hasNext()).isFalse();
    }

    @Test
    public void canAggregateWithManyOperators() {

        articles = collection.aggregate("{$match:{tags:'virus'}}").and("{$limit:1}").as(Article.class);

        articles.next();
        assertThat(articles.hasNext()).isFalse();
    }

    @Test
    public void shouldCheckIfCommandHasErrors() {

        try {
            collection.aggregate("{$invalid:{}}").as(Article.class);
            fail();
        } catch (Exception e) {
            assertThat(MongoCommandException.class).isAssignableFrom(e.getClass());
        }
    }

    @Test
    public void shouldPopulateIds() throws IOException {
        ResultsIterator<Friend> resultsIterator = friendCollection.aggregate("{$project: {_id: '$_id', name: '$name'}}")
                .as(Friend.class);
        List<Friend> friends = Lists.newArrayList(
                (Iterable<Friend>) resultsIterator);

        assertThat(friends.isEmpty()).isEqualTo(false);
        for (Friend friend : friends) {
            assertThat(friend.getId()).isNotNull();
        }

        resultsIterator.close();
    }

    @Test
    public void shouldUnmarshalNestedDocuments() throws IOException {
        ResultsIterator<NestedDocument> resultsIterator = nestedCollection
                .aggregate("{$unwind: '$nested'}")
                .and("{$project: {name: '$nested.name', value: '$nested.value'}}")
                .as(NestedDocument.class);
        List<NestedDocument> nested = Lists.newArrayList((Iterable<NestedDocument>) resultsIterator);

        assertThat(nested.isEmpty()).isEqualTo(false);
        assertThat(nested.get(0)).isEqualToComparingFieldByField(new NestedDocument().withName("name1").withValue("value1"));
        assertThat(nested.get(1)).isEqualToComparingFieldByField(new NestedDocument().withName("name2").withValue("value2"));
        assertThat(nested.get(2)).isEqualToComparingFieldByField(new NestedDocument().withName("name3").withValue("value3"));
        assertThat(nested.get(3)).isEqualToComparingFieldByField(new NestedDocument().withName("name4").withValue("value4"));

        resultsIterator.close();
    }

}
