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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

import java.util.ArrayList;
import java.util.List;

public class Distinct {
    private final DBCollection dbCollection;
    private final Unmarshaller unmarshaller;
    private final String key;
    private Query query;
    private final QueryFactory queryFactory;

    Distinct(DBCollection dbCollection, Unmarshaller unmarshaller, QueryFactory queryFactory, String key) {
        this.dbCollection = dbCollection;
        this.unmarshaller = unmarshaller;
        this.key = key;
        this.queryFactory = queryFactory;
        this.query = this.queryFactory.createQuery("{}");
    }

    public Distinct query(String query) {
        this.query = queryFactory.createQuery(query);
        return this;
    }

    public Distinct query(String query, Object... parameters) {
        this.query = queryFactory.createQuery(query, parameters);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> as(final Class<T> clazz) {
        DBObject ref = query.toDBObject();
        final List<?> distinct = dbCollection.distinct(key, ref);

        if (distinct.isEmpty() || resultsAreBSONPrimitive(distinct))
            return (List<T>) distinct;
        else {
            return typedList((List<DBObject>) distinct, clazz);
        }
    }

    public <T> List<T> map(ResultHandler<T> resultHandler) {
        DBObject ref = query.toDBObject();
        final List<?> distinct = dbCollection.distinct(key, ref);

        if (distinct.isEmpty() || resultsAreBSONPrimitive(distinct)) {
            List<DBObject> objects = new ArrayList();
            for(Object object:distinct) {
                objects.add(new BasicDBObject(key, object));
            }
            return typedList(objects, resultHandler);
        } else return typedList((List<DBObject>) distinct, resultHandler);
    }

    private boolean resultsAreBSONPrimitive(List<?> distinct) {
        return !(distinct.get(0) instanceof DBObject);
    }

    private <T> List<T> typedList(List<DBObject> distinct, Class<T> clazz) {
        List<T> results = new ArrayList<T>();
        ResultHandler<T> handler = ResultHandlerFactory.newResultHandler(clazz, unmarshaller);
        for (DBObject dbObject : distinct) {
            results.add(handler.map(dbObject));
        }
        return results;
    }

    private <T> List<T> typedList(List<DBObject> distinct, ResultHandler<T> handler) {
        List<T> results = new ArrayList<T>();
        for (DBObject dbObject : distinct) {
            results.add(handler.map(dbObject));
        }
        return results;
    }

}
