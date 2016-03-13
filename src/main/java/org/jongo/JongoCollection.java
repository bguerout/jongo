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

import com.mongodb.BasicDBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jongo.query.Query;


public class JongoCollection {

    public static final String MONGO_DOCUMENT_ID_NAME = "_id";
    public static final String MONGO_QUERY_OID = "$oid";
    private static final Object[] NO_PARAMETERS = {};
    private static final String ALL = "{}";

    private final MongoCollection<BasicDBObject> collection;
    private final WriteConcern writeConcern;
    private final ReadPreference readPreference;
    private final Mapper mapper;

    public JongoCollection(MongoCollection<BasicDBObject> dbCollection, Mapper mapper) {
        this(dbCollection, mapper, dbCollection.getWriteConcern(), dbCollection.getReadPreference());

    }

    private JongoCollection(MongoCollection<BasicDBObject> dbCollection, Mapper mapper, WriteConcern writeConcern, ReadPreference readPreference) {
        this.collection = dbCollection;
        this.writeConcern = writeConcern;
        this.readPreference = readPreference;
        this.mapper = mapper;
    }

    public JongoCollection withWriteConcern(WriteConcern concern) {
        return new JongoCollection(collection, mapper, concern, readPreference);
    }

    public JongoCollection withReadPreference(ReadPreference readPreference) {
        return new JongoCollection(collection, mapper, writeConcern, readPreference);
    }

    public FindOne findOne(ObjectId id) {
        if (id == null) {
            throw new IllegalArgumentException("Object id must not be null");
        }
        return new FindOne(collection, readPreference, mapper.getUnmarshaller(), mapper.getQueryFactory(), "{_id:#}", id);
    }

    public FindOne findOne() {
        return findOne(ALL);
    }

    public FindOne findOne(String query) {
        return findOne(query, NO_PARAMETERS);
    }

    public FindOne findOne(String query, Object... parameters) {
        return new FindOne(collection, readPreference, mapper.getUnmarshaller(), mapper.getQueryFactory(), query, parameters);
    }

    public Find find() {
        return find(ALL);
    }

    public Find find(String query) {
        return find(query, NO_PARAMETERS);
    }

    public Find find(String query, Object... parameters) {
        return new Find(collection, readPreference, mapper.getUnmarshaller(), mapper.getQueryFactory(), query, parameters);
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
        Bson dbQuery = createQuery(query, parameters).toBson();
        return collection.count(dbQuery, new CountOptions());
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

    public WriteResult save(Object pojo) {
        return new Insert(collection, writeConcern, mapper.getMarshaller(), mapper.getObjectIdUpdater(), mapper.getQueryFactory()).save(pojo);
    }

    public WriteResult insert(Object pojo) {
        return insert(new Object[]{pojo});
    }

    public WriteResult insert(String query) {
        return insert(query, NO_PARAMETERS);
    }

    public WriteResult insert(Object... pojos) {
        return new Insert(collection, writeConcern, mapper.getMarshaller(), mapper.getObjectIdUpdater(), mapper.getQueryFactory()).insert(pojos);
    }

    public WriteResult insert(String query, Object... parameters) {
        return new Insert(collection, writeConcern, mapper.getMarshaller(), mapper.getObjectIdUpdater(), mapper.getQueryFactory()).insert(query, parameters);
    }

    public DeleteResult remove(ObjectId id) {
        return remove("{" + MONGO_DOCUMENT_ID_NAME + ":#}", id);
    }

    public DeleteResult remove() {
        return remove(ALL);
    }

    public DeleteResult remove(String query) {
        return remove(query, NO_PARAMETERS);
    }

    public DeleteResult remove(String query, Object... parameters) {
        return collection.deleteOne(createQuery(query, parameters).toBson());
    }

    public Distinct distinct(String key) {
        return new Distinct(collection, mapper.getUnmarshaller(), mapper.getQueryFactory(), key);
    }

    public Aggregate aggregate(String pipelineOperator) {
        return aggregate(pipelineOperator, NO_PARAMETERS);
    }

    public Aggregate aggregate(String pipelineOperator, Object... parameters) {
        return new Aggregate(collection, mapper.getUnmarshaller(), mapper.getQueryFactory()).and(pipelineOperator, parameters);
    }

    public void drop() {
        collection.drop();
    }

    public void dropIndex(String keys) {
        collection.dropIndex(createQuery(keys).toBson());
    }

    public void dropIndexes() {
        collection.dropIndexes();
    }

    public void ensureIndex(String keys) {
        collection.createIndex(createQuery(keys).toBson());
    }

    public void ensureIndex(String keys, String options) {
        collection.createIndex(createQuery(keys).toBson(), new IndexOptions());
    }

    public String getName() {
        return collection.getNamespace().getCollectionName()
    }

    public MongoCollection<BasicDBObject> getDBCollection() {
        return collection;
    }

    private Query createQuery(String query, Object... parameters) {
        return mapper.getQueryFactory().createQuery(query, parameters);
    }

    @Override
    public String toString() {
        if (collection != null)
            return "collection {" + "name: '" + getName() + "', db: '" + collection.getNamespace().getDatabaseName() + "'}";
        else
            return super.toString();
    }
}
