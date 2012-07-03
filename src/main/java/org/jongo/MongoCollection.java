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


public final class MongoCollection {

    private final DBCollection collection;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    private final QueryFactory queryFactory;

    MongoCollection(DBCollection dbCollection, Marshaller marshaller, Unmarshaller unmarshaller) {
        this.collection = dbCollection;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        this.queryFactory = new QueryFactory(marshaller);
    }

    private static final Object[] NO_PARAMETERS = {};

    public FindOne findOne(ObjectId id) {
        if (id == null) {
            throw new IllegalArgumentException("Object id must not be null");
        }
        return new FindOne(collection, unmarshaller, queryFactory, "{_id:#}", id);
    }

    public FindOne findOne() {
        return findOne("{}");
    }

    public FindOne findOne(String query) {
        return findOne(query, NO_PARAMETERS);
    }

    public FindOne findOne(String query, Object... parameters) {
        return new FindOne(collection, unmarshaller, queryFactory, query, parameters);
    }

    public Find find() {
        return find("{}");
    }

    public Find find(String query) {
        return find(query, NO_PARAMETERS);
    }

    public Find find(String query, Object... parameters) {
        return new Find(collection, unmarshaller, queryFactory, query, parameters);
    }

    public long count() {
        return collection.count();
    }

    public long count(String query) {
        return count(query, NO_PARAMETERS);
    }

    public long count(String query, Object... parameters) {
        DBObject dbQuery = createQuery(query, parameters).toDBObject();
        return collection.count(dbQuery);
    }

    public Update update(String query) {
        return update(query, NO_PARAMETERS);
    }

    public Update update(String query, Object... parameters) {
        return new Update(collection, queryFactory, query, parameters);
    }

    public WriteResult save(Object document) {
        return new Save(collection, marshaller, document).execute();
    }

    public WriteResult save(Object document, WriteConcern concern) {
        return new Save(collection, marshaller, document).concern(concern).execute();
    }

    public WriteResult insert(String query) {
        return insert(query, new Object[0]);
    }

    public WriteResult insert(String query, Object... parameters) {
        DBObject dbQuery = createQuery(query, parameters).toDBObject();
        return collection.insert(dbQuery);
    }

    public WriteResult remove(ObjectId id) {
        return remove("{_id:#}", id);
    }

    public WriteResult remove(String query) {
        return remove(query, new Object[0]);
    }

    public WriteResult remove(String query, Object... parameters) {
        return collection.remove(createQuery(query, parameters).toDBObject());
    }

    public Distinct distinct(String key) {
        return new Distinct(collection, unmarshaller, queryFactory, key);
    }

    public Aggregate aggregate(String pipelineOperator) {
        return aggregate(pipelineOperator, new Object[0]);
    }

    public Aggregate aggregate(String pipelineOperator, Object... parameters) {
        return new Aggregate(collection.getDB(), collection.getName(), unmarshaller, queryFactory).and(pipelineOperator, parameters);
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

    private Query createQuery(String query, Object... parameters) {
        return queryFactory.createQuery(query, parameters);
    }

    @Override
    public String toString() {
        if (collection != null)
            return "collection {" + "name: '" + collection.getName() + "', db: '" + collection.getDB().getName() + "'}";
        else
            return super.toString();
    }
}
