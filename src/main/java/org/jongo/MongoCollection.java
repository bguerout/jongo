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
import org.jongo.query.Query;


public class MongoCollection {

    public static final String MONGO_DOCUMENT_ID_NAME = "_id";
    public static final String MONGO_QUERY_OID = "$oid";
    private static final Object[] NO_PARAMETERS = {};
    private static final String ALL = "{}";

    private final DBCollection collection;
    private final WriteConcern writeConcern;
    private final Mapper mapper;

    public MongoCollection(DBCollection dbCollection, Mapper mapper) {
        this(dbCollection, mapper, dbCollection.getWriteConcern());

    }

    private MongoCollection(DBCollection dbCollection, Mapper mapper, WriteConcern writeConcern) {
        this.collection = dbCollection;
        this.writeConcern = writeConcern;
        this.mapper = mapper;
    }

    public MongoCollection withConcern(WriteConcern concern) {
        return new MongoCollection(collection, mapper, concern);
    }

    public FindOne findOne(ObjectId id) {
        if (id == null) {
            throw new IllegalArgumentException("Object id must not be null");
        }
        return new FindOne(collection, mapper.getUnmarshaller(), mapper.getQueryFactory(), "{_id:#}", id);
    }

    public FindOne findOne() {
        return findOne(ALL);
    }

    public FindOne findOne(String query) {
        return findOne(query, NO_PARAMETERS);
    }

    public FindOne findOne(String query, Object... parameters) {
        return new FindOne(collection, mapper.getUnmarshaller(), mapper.getQueryFactory(), query, parameters);
    }

    public Find find() {
        return find(ALL);
    }

    public Find find(String query) {
        return find(query, NO_PARAMETERS);
    }

    public Find find(String query, Object... parameters) {
        return new Find(collection, mapper.getUnmarshaller(), mapper.getQueryFactory(), query, parameters);
    }

    public FindAndModify findAndModify() {
        return findAndModify(ALL);
    }

    public FindAndModify findAndModify(String query) {
        return findAndModify(query, NO_PARAMETERS);
    }

    public FindAndModify findAndModify(String query, Object... parameters) {
        return new FindAndModify(collection, mapper.getUnmarshaller(), mapper.getQueryFactory(), query, parameters);
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

    public Update update(ObjectId id) {
        if (id == null) {
            throw new IllegalArgumentException("Object id must not be null");
        }
        return update("{_id:#}", id);
    }

    public Update update(String query, Object... parameters) {
        return new Update(collection, writeConcern, mapper.getQueryFactory(), query, parameters);
    }

    public WriteResult save(Object document) {
        return new Save(collection, writeConcern, mapper.getMarshaller(), mapper.getObjectIdUpdater(), document).execute();
    }

    public WriteResult insert(String query) {
        return insert(query, NO_PARAMETERS);
    }

    public WriteResult insert(String query, Object... parameters) {
        DBObject dbQuery = createQuery(query, parameters).toDBObject();
        return collection.insert(dbQuery, writeConcern);
    }

    public WriteResult remove(ObjectId id) {
        return remove("{" + MONGO_DOCUMENT_ID_NAME + ":#}", id);
    }

    public WriteResult remove() {
        return remove(ALL);
    }

    public WriteResult remove(String query) {
        return remove(query, NO_PARAMETERS);
    }

    public WriteResult remove(String query, Object... parameters) {
        return collection.remove(createQuery(query, parameters).toDBObject(), writeConcern);
    }

    public Distinct distinct(String key) {
        return new Distinct(collection, mapper.getUnmarshaller(), mapper.getQueryFactory(), key);
    }

    public Aggregate aggregate(String pipelineOperator) {
        return aggregate(pipelineOperator, NO_PARAMETERS);
    }

    public Aggregate aggregate(String pipelineOperator, Object... parameters) {
        return new Aggregate(collection.getDB(), collection.getName(), mapper.getUnmarshaller(), mapper.getQueryFactory()).and(pipelineOperator, parameters);
    }

    public void drop() {
        collection.drop();
    }

    public void dropIndex(String keys) {
        collection.dropIndex(createQuery(keys).toDBObject());
    }

    public void dropIndexes() {
        collection.dropIndexes();
    }

    public void ensureIndex(String keys) {
        collection.ensureIndex(createQuery(keys).toDBObject());
    }

    public void ensureIndex(String keys, String options) {
        collection.ensureIndex(createQuery(keys).toDBObject(), createQuery(options).toDBObject());
    }

    public String getName() {
        return collection.getName();
    }

    public DBCollection getDBCollection() {
        return collection;
    }

    private Query createQuery(String query, Object... parameters) {
        return mapper.getQueryFactory().createQuery(query, parameters);
    }

    @Override
    public String toString() {
        if (collection != null)
            return "collection {" + "name: '" + collection.getName() + "', db: '" + collection.getDB().getName() + "'}";
        else
            return super.toString();
    }
}
