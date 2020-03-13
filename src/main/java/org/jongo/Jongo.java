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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoDatabase;
import org.jongo.bson.BsonDBDecoder;
import org.jongo.bson.BsonDBEncoder;
import org.jongo.query.Query;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class Jongo {

    private final DB database;
    private final Mapper mapper;

    @Deprecated
    public Jongo(DB database) {
        this(database, jacksonMapper().build());
    }

    @Deprecated
    public Jongo(DB database, Mapper mapper) {
        this.database = database;
        this.mapper = mapper;
    }

    public MongoCollection getCollection(String name) {
        DBCollection dbCollection = database.getCollection(name);
        dbCollection.setDBDecoderFactory(BsonDBDecoder.FACTORY);
        dbCollection.setDBEncoderFactory(BsonDBEncoder.FACTORY);
        dbCollection.setReadConcern(database.getReadConcern());
        return new MongoCollection(dbCollection, mapper);
    }

    public DB getDatabase() {
        return database;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public Query createQuery(String query, Object... parameters) {
        return mapper.getQueryFactory().createQuery(query, parameters);
    }

    public Command runCommand(String query) {
        return runCommand(query, new Object[0]);
    }

    public Command runCommand(String query, Object... parameters) {
        return new Command(database, mapper.getUnmarshaller(), mapper.getQueryFactory(), query, parameters);
    }

    public static JongoNative useNative(MongoDatabase database) {
        return new JongoNative(database);
    }

    public static JongoNative useNative(MongoDatabase database, Mapper mapper) {
        return new JongoNative(database, mapper);
    }
}
