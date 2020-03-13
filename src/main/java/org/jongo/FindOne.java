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
import com.mongodb.ReadPreference;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

import static org.jongo.ResultHandlerFactory.newResultHandler;

public class FindOne {

    private final Unmarshaller unmarshaller;
    private final DBCollection collection;
    private final ReadPreference readPreference;
    private final Query query;
    private Query fields, orderBy;
    private final QueryFactory queryFactory;

    FindOne(DBCollection collection, ReadPreference readPreference, Unmarshaller unmarshaller, QueryFactory queryFactory, String query, Object... parameters) {
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.readPreference = readPreference;
        this.queryFactory = queryFactory;
        this.query = this.queryFactory.createQuery(query, parameters);
    }

    public <T> T as(final Class<T> clazz) {
        return map(newResultHandler(clazz, unmarshaller));
    }

    public <T> T map(ResultHandler<T> resultHandler) {
        DBObject result = collection.findOne(query.toDBObject(), getFieldsAsDBObject(), getOrderByAsDBObject(), readPreference);
        return result == null ? null : resultHandler.map(result);
    }

    public FindOne projection(String fields) {
        this.fields = queryFactory.createQuery(fields);
        return this;
    }

    public FindOne projection(String fields, Object... parameters) {
        this.fields = queryFactory.createQuery(fields, parameters);
        return this;
    }

    public FindOne orderBy(String orderBy) {
        this.orderBy = queryFactory.createQuery(orderBy);
        return this;
    }

    private DBObject getFieldsAsDBObject() {
        return fields == null ? null : fields.toDBObject();
    }

    private DBObject getOrderByAsDBObject() {
        return orderBy == null ? null : orderBy.toDBObject();
    }
}
