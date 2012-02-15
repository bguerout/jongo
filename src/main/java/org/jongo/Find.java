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
import org.jongo.marshall.Unmarshaller;

import java.util.Iterator;

import static org.jongo.Jongo.toDBObject;
import static org.jongo.ResultMapperFactory.newMapper;

public class Find {

    private final DBCollection collection;
    private final Unmarshaller unmarshaller;
    private Query query;
    private DBObject fields;
    private Integer limit, skip;
    private DBObject sort;

    Find(Unmarshaller unmarshaller, DBCollection collection, Query query) {
        this.unmarshaller = unmarshaller;
        this.collection = collection;
        this.query = query;
    }

    public Find on(String fields) {
        this.fields = toDBObject(fields);
        return this;
    }

    public <T> Iterator<T> as(final Class<T> clazz) {
        return map(newMapper(clazz, unmarshaller));
    }

    public <T> Iterator<T> map(ResultMapper<T> resultMapper) {
        DBCursor cursor = collection.find(query.toDBObject(), fields);
        addOptionsOn(cursor);
        return new MongoIterator<T>(cursor, resultMapper);
    }

    private void addOptionsOn(DBCursor cursor) {
        if (limit != null)
            cursor.limit(limit);
        if (skip != null)
            cursor.skip(skip);
        if (sort != null) {
            cursor.sort(sort);
        }
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
        this.sort = toDBObject(sort);
        return this;
    }
}
