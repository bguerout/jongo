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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.jongo.ResultHandlerFactory.newResultHandler;

public class Find {

    private final DBCollection collection;
    private final ReadPreference readPreference;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;
    private final Query query;
    private final List<QueryModifier> modifiers;
    private Query fields;

    Find(DBCollection collection, ReadPreference readPreference, Unmarshaller unmarshaller, QueryFactory queryFactory, String query, Object... parameters) {
        this.readPreference = readPreference;
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.queryFactory = queryFactory;
        this.query = this.queryFactory.createQuery(query, parameters);
        this.modifiers = new ArrayList<QueryModifier>();
    }

    public <T> MongoCursor<T> as(final Class<T> clazz) {
        return map(newResultHandler(clazz, unmarshaller));
    }

    public <T> MongoCursor<T> map(ResultHandler<T> resultHandler) {
        DBCursor cursor = new DBCursor(collection, query.toDBObject(), getFieldsAsDBObject(), readPreference);
        for (QueryModifier modifier : modifiers) {
            modifier.modify(cursor);
        }
        return new MongoCursor<T>(cursor, resultHandler);
    }

    public Find projection(String fields) {
        this.fields = queryFactory.createQuery(fields);
        return this;
    }

    public Find projection(String fields, Object... parameters) {
        this.fields = queryFactory.createQuery(fields, parameters);
        return this;
    }

    public Find limit(final int limit) {
        this.modifiers.add(new QueryModifier() {
            public void modify(DBCursor cursor) {
                cursor.limit(limit);
            }
        });
        return this;
    }

    public Find skip(final int skip) {
        this.modifiers.add(new QueryModifier() {
            public void modify(DBCursor cursor) {
                cursor.skip(skip);
            }
        });
        return this;
    }

    public Find sort(String sort) {
        final DBObject sortDBObject = queryFactory.createQuery(sort).toDBObject();
        this.modifiers.add(new QueryModifier() {
            public void modify(DBCursor cursor) {
                cursor.sort(sortDBObject);
            }
        });
        return this;
    }

    public Find hint(String hint) {
        final DBObject hintDBObject = queryFactory.createQuery(hint).toDBObject();
        this.modifiers.add(new QueryModifier() {
            public void modify(DBCursor cursor) {
                cursor.hint(hintDBObject);
            }
        });
        return this;
    }

    public Find with(QueryModifier queryModifier) {
        this.modifiers.add(queryModifier);
        return this;
    }

    private DBObject getFieldsAsDBObject() {
        return fields == null ? null : fields.toDBObject();
    }

}
