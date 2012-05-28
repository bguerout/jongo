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

import java.util.Iterator;
import java.util.List;

import static org.jongo.ResultMapperFactory.newMapper;

class Distinct {

    private final DBCollection collection;
    private String key;
    private final Query query;
    private final Unmarshaller unmarshaller;

    Distinct(DBCollection collection, Unmarshaller unmarshaller, String key, Query query) {
        this.collection = collection;
        this.key = key;
        this.query = query;
        this.unmarshaller = unmarshaller;
    }

    public <T> Iterable<T> as(Class<T> clazz) {
        DBObject ref = query.toDBObject();
        final List<?> distinct = collection.distinct(key, ref);
        if (BSONPrimitives.contains(clazz))
            return new Iterable<T>() {
                public Iterator<T> iterator() {
                    return (Iterator<T>) distinct.iterator();
                }
            };
        else
            return new MongoIterator<T>((Iterator<DBObject>) distinct.iterator(), newMapper(clazz, unmarshaller));
    }
}
