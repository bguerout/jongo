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
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

public interface MongoCollection {
    FindOne findOne(String query);

    FindOne findOne(ObjectId id);

    FindOne findOne(String query, Object... parameters);

    Find find(String query);

    Find find(String query, Object... parameters);

    long count();

    long count(String query);

    long count(String query, Object... parameters);

    WriteResult update(String query, String modifier);

    WriteResult update(String query, String modifier, WriteConcern concern);

    WriteResult upsert(String query, String modifier);

    WriteResult upsert(String query, String modifier, WriteConcern concern);

    <D> String save(D document);

    <D> String save(D document, WriteConcern concern);

    WriteResult insert(String query);

    WriteResult insert(String query, Object... parameters);

    WriteResult remove(String query);

    WriteResult remove(String query, Object... parameters);

    WriteResult remove(ObjectId id);

    @SuppressWarnings("unchecked")
    <T> Iterable<T> distinct(String key, String query, Class<T> clazz);

    void drop();

    void ensureIndex(String index);

    String getName();

    DBCollection getDBCollection();
}
