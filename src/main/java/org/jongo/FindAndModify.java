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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

import static org.jongo.ResultHandlerFactory.newResultHandler;

public class FindAndModify {

    private final DBCollection collection;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;
    private final Query query;
    private Query fields, sort, modifier;
    private boolean remove = false;
    private boolean returnNew = false;
    private boolean upsert = false;

    FindAndModify(DBCollection collection, Unmarshaller unmarshaller, QueryFactory queryFactory, String query, Object... parameters) {
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.queryFactory = queryFactory;
        this.query = this.queryFactory.createQuery(query, parameters);
    }

    public FindAndModify with(String modifier, Object... parameters) {
        if (modifier == null) throw new IllegalArgumentException("Modifier may not be null");
        this.modifier = queryFactory.createQuery(modifier, parameters);
        return this;
    }

    public <T> T as(final Class<T> clazz) {
        return map(newResultHandler(clazz, unmarshaller));
    }

    public <T> T map(ResultHandler<T> resultHandler) {
        DBObject result = collection.findAndModify(query.toDBObject(),
                getAsDBObject(fields),
                getAsDBObject(sort),
                remove,
                getAsDBObject(modifier),
                returnNew,
                upsert);

        return result == null ? null : resultHandler.map(result);
    }

    public FindAndModify projection(String fields) {
        this.fields = queryFactory.createQuery(fields);
        return this;
    }

    public FindAndModify projection(String fields, Object... parameters) {
        this.fields = queryFactory.createQuery(fields, parameters);
        return this;
    }

    public FindAndModify sort(String sort) {
        this.sort = queryFactory.createQuery(sort);
        return this;
    }

    public FindAndModify remove() {
        this.remove = true;
        return this;
    }

    public FindAndModify returnNew() {
        this.returnNew = true;
        return this;
    }

    public FindAndModify upsert() {
        this.upsert = true;
        return this;
    }

    private DBObject getAsDBObject(Query query) {
        return query == null ? null : query.toDBObject();
    }
}
