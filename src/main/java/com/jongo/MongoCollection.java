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

package com.jongo;

import com.jongo.jackson.JsonProcessor;
import com.mongodb.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

public class MongoCollection {

    public static final String MONGO_ID = "_id";
    private final DBCollection collection;
    private final JsonProcessor jsonProcessor;

    public MongoCollection(String database, String collection) throws UnknownHostException, MongoException {
        this.collection = new Mongo().getDB(database).getCollection(collection);
        this.jsonProcessor = new JsonProcessor();
    }

    public <T> T findOne(String query, Class<T> clazz) {
        return findOne(query, jsonProcessor.createMapper(clazz));
    }

    public <T> T findOne(String query, Object[] parameters, Class<T> clazz) {
        return findOne(query, parameters, jsonProcessor.createMapper(clazz));
    }

    public <T> T findOne(String query, DBObjectMapper<T> resultMapper) {
        Query staticQuery = new StaticQuery(jsonProcessor, query);
        return findOne(staticQuery.toDBObject(), resultMapper);
    }

    public <T> T findOne(String query, Object[] parameters, DBObjectMapper<T> resultMapper) {
        Query parameterizedQuery = new ParameterizedQuery(jsonProcessor, query, parameters);
        return findOne(parameterizedQuery.toDBObject(), resultMapper);
    }

    private <T> T findOne(DBObject queryAsDBObject, DBObjectMapper<T> dbObjectMapper) {
        DBObject result = collection.findOne(queryAsDBObject);
        if (result == null)
            return null;
        else
            return dbObjectMapper.map(result);
    }


    public <T> Iterator<T> find(String query, Class<T> clazz) {
        return find(query, jsonProcessor.createMapper(clazz));
    }

    public <T> Iterator<T> find(String query, DBObjectMapper<T> dbObjectMapper) {
        return find(jsonProcessor.toDBObject(query), dbObjectMapper);
    }

    public <T> Iterator<T> find(String query, Object[] parameters, DBObjectMapper<T> dbObjectMapper) {
        Query parameterizedQuery = new ParameterizedQuery(jsonProcessor, query, parameters);
        return find(parameterizedQuery.toDBObject(), dbObjectMapper);
    }

    private <T> Iterator<T> find(DBObject queryAsDBObject, DBObjectMapper<T> dbObjectMapper) {
        DBCursor cursor = collection.find(queryAsDBObject);
        return new MongoIterator(cursor, dbObjectMapper);
    }

    public long count(String query) {
        DBObject ref = jsonProcessor.toDBObject(query);
        return collection.count(ref);
    }

    @SuppressWarnings("unchecked")
    public <T> Iterator<T> distinct(String key, String query, Class<T> clazz) {
        DBObject ref = jsonProcessor.toDBObject(query);
        List<?> distinct = collection.distinct(key, ref);
        if (BSONPrimitives.contains(clazz))
            return (Iterator<T>) distinct.iterator();
        else
            return new MongoIterator((Iterator<DBObject>) distinct.iterator(), jsonProcessor.createMapper(clazz));
    }

    public <D> void save(D document) throws IOException {
        collection.save(jsonProcessor.toDBObject(document));
    }

    public void drop() {
        collection.drop();
    }


}
