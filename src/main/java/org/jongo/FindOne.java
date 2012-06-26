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
import com.mongodb.DBObject;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;

import static org.jongo.ResultMapperFactory.newMapper;

public final class FindOne {

    private final Unmarshaller unmarshaller;
    private final DBCollection collection;
    private final Query query;
    private Query fields;
    private final QueryFactory queryFactory;

    FindOne(DBCollection collection, Unmarshaller unmarshaller, QueryFactory queryFactory, String query, Object... parameters) {
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.queryFactory = queryFactory;
        this.query = this.queryFactory.createQuery(query, parameters);
    }

    public FindOne fields(String fields) {
        this.fields = queryFactory.createQuery(fields);
        return this;
    }

    public <T> T as(final Class<T> clazz) {
        return map(newMapper(clazz, unmarshaller));
    }

    public <T> T map(ResultMapper<T> resultMapper) {
        DBObject result = collection.findOne(query.toDBObject(), getFieldsAsDBObject());
        if (result == null)
            return null;

        return resultMapper.map(result);
    }

    private DBObject getFieldsAsDBObject() {
        return fields == null ? null : fields.toDBObject();
    }

}
