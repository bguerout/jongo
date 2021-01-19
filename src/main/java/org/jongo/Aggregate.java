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

import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import static org.jongo.ResultHandlerFactory.newResultHandler;

public class Aggregate {

    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;
    private final List<DBObject> pipeline;
    private final AtomicReference<AggregationOptions> options;
    private final DBCollection collection;

    Aggregate(DBCollection collection, Unmarshaller unmarshaller, QueryFactory queryFactory) {
        this.unmarshaller = unmarshaller;
        this.queryFactory = queryFactory;
        this.pipeline = new ArrayList<DBObject>();
        this.options = new AtomicReference<AggregationOptions>();
        this.collection = collection;
    }

    public Aggregate and(String pipelineOperator, Object... parameters) {
        DBObject dbQuery = queryFactory.createQuery(pipelineOperator, parameters).toDBObject();
        pipeline.add(dbQuery);
        return this;
    }

    public <T> ResultsIterator<T> as(final Class<T> clazz) {
        return map(newResultHandler(clazz, unmarshaller));
    }

    public Aggregate options(AggregationOptions options) {
        this.options.set(options);
        return this;
    }

    public <T> ResultsIterator<T> map(ResultHandler<T> resultHandler) {
        Iterator<DBObject> results;
        AggregationOptions options = this.options.get();
        if (options != null) {
            results = collection.aggregate(pipeline, options);
        } else {
            results = collection.aggregate(pipeline, AggregationOptions.builder().build());
        }
        return new ResultsIterator<T>(results, resultHandler);
    }

    public static class ResultsIterator<E> implements Iterator<E>, Iterable<E>, Closeable {

        private Iterator<DBObject> results;
        private ResultHandler<E> resultHandler;

        private ResultsIterator(Iterator<DBObject> results, ResultHandler<E> resultHandler) {
            this.resultHandler = resultHandler;
            this.results = results;
        }

        public Iterator<E> iterator() {
            return this;
        }

        public boolean hasNext() {
            return results.hasNext();
        }

        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();

            DBObject dbObject = results.next();
            return resultHandler.map(dbObject);
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() method is not supported");
        }

        public void close() throws IOException {
            if (results instanceof Closeable) {
                Closeable closeable = (Closeable) results;
                closeable.close();
            }
        }

        boolean isCursor() {
            return (results instanceof Cursor);
        }
    }
}
