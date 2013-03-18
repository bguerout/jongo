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

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

import static org.jongo.ResultHandlerFactory.newMapper;

public class Find {

    private final DBCollection collection;
    private final ReadPreference readPreference;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;
    private final Query query;
    private Query fields, sort;
    private Integer limit, skip;

    Find(DBCollection collection, ReadPreference readPreference, Unmarshaller unmarshaller, QueryFactory queryFactory, String query, Object... parameters) {
        this.readPreference = readPreference;
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.queryFactory = queryFactory;
        this.query = this.queryFactory.createQuery(query, parameters);
    }

    public <T> Iterable<T> as(final Class<T> clazz) {
        return map(newMapper(clazz, unmarshaller));
    }

    public <T> Iterable<T> map(ResultHandler<T> resultHandler) {
        DBCursor cursor = new DBCursor(collection, query.toDBObject(), getFieldsAsDBObject(), readPreference);
        addOptionsOn(cursor);
        return new MongoIterator<T>(cursor, resultHandler);
    }

    private void addOptionsOn(DBCursor cursor) {
        if (limit != null)
            cursor.limit(limit);
        if (skip != null)
            cursor.skip(skip);
        if (sort != null) {
            cursor.sort(sort.toDBObject());
        }
    }

    public Find projection(String fields) {
        this.fields = queryFactory.createQuery(fields);
        return this;
    }

    public Find projection(String fields, Object... parameters) {
        this.fields = queryFactory.createQuery(fields, parameters);
        return this;
    }

    public Find limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Find skip(int skip) {
        this.skip = skip;
        return this;
    }

    public Find sort(String sort) {
        this.sort = queryFactory.createQuery(sort);
        return this;
    }

    private DBObject getFieldsAsDBObject() {
        return fields == null ? null : fields.toDBObject();
    }
}
