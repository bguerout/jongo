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
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.jongo.query.QueryFactory;


class DefaultMongoCollection implements MongoCollection {

    private final DBCollection collection;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;

    DefaultMongoCollection(DBCollection dbCollection, Marshaller marshaller, Unmarshaller unmarshaller) {
        this.collection = dbCollection;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        this.queryFactory = new QueryFactory();
    }

    public FindOne findOne(String query) {
        return new FindOne(collection, createQuery(query), unmarshaller);
    }

    public FindOne findOne(ObjectId id) {
        if (id == null) {
            throw new IllegalArgumentException("Object id must not be null");
        }
        return new FindOne(collection, createQuery("{_id:#}", id), unmarshaller);
    }

    public FindOne findOne(String query, Object... parameters) {
        return new FindOne(collection, createQuery(query, parameters), unmarshaller);
    }

    public Find find(String query) {
        return new Find(collection, createQuery(query), unmarshaller);
    }

    public Find find(String query, Object... parameters) {
        return new Find(collection, createQuery(query, parameters), unmarshaller);
    }

    public long count() {
        return collection.count();
    }

    public long count(String query) {
        return collection.count(createQuery(query).toDBObject());
    }

    public long count(String query, Object... parameters) {
        return collection.count(createQuery(query, parameters).toDBObject());
    }

    public WriteResult update(String query, String modifier) {
        return update(query, modifier, collection.getWriteConcern());
    }

    public WriteResult update(String query, String modifier, WriteConcern concern) {
        return update(query, modifier, false, true, concern);
    }

    public WriteResult upsert(String query, String modifier) {
        return upsert(query, modifier, collection.getWriteConcern());
    }

    public WriteResult upsert(String query, String modifier, WriteConcern concern) {
        return update(query, modifier, true, false, concern);
    }

    private WriteResult update(String query, String modifier, boolean upsert, boolean multi, WriteConcern concern) {
        DBObject dbQuery = createQuery(query).toDBObject();
        DBObject dbModifier = createQuery(modifier).toDBObject();
        return collection.update(dbQuery, dbModifier, upsert, multi, concern);
    }

    public <D> String save(D document) {
        return save(document, collection.getWriteConcern());
    }

    public <D> String save(D document, WriteConcern concern) {
        return new Save(collection, marshaller).execute(document, concern);
    }

    public WriteResult insert(String query) {
        DBObject dbQuery = createQuery(query).toDBObject();
        return collection.save(dbQuery);
    }

    public WriteResult insert(String query, Object... parameters) {
        DBObject dbQuery = createQuery(query, parameters).toDBObject();
        return collection.save(dbQuery);
    }

    public WriteResult remove(String query) {
        DBObject dbQuery = createQuery(query).toDBObject();
        return collection.remove(dbQuery);
    }

    public WriteResult remove(String query, Object... parameters) {
        return collection.remove(createQuery(query, parameters).toDBObject());
    }

    public WriteResult remove(ObjectId id) {
        return remove("{_id:#}", id);
    }

    @SuppressWarnings("unchecked")
    public <T> Iterable<T> distinct(String key, String query, final Class<T> clazz) {
        return new Distinct(collection, unmarshaller, key, createQuery(query)).as(clazz);
    }

    public void drop() {
        collection.drop();
    }

    public void ensureIndex(String index) {
        DBObject dbIndex = createQuery(index).toDBObject();
        collection.ensureIndex(dbIndex);
    }

    public String getName() {
        return collection.getName();
    }

    public DBCollection getDBCollection() {
        return collection;
    }

    private Query createQuery(String query) {
        return queryFactory.createQuery(query);
    }

    private Query createQuery(String query, Object... parameters) {
        return queryFactory.createQuery(query, parameters);
    }


}
