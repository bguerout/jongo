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


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.jongo.query.Query;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class JongoNative {

    private final Mapper mapper;
    private final CodecRegistry codecRegistry;
    private final MongoDatabase database;

    public JongoNative(MongoDatabase database) {
        this(database, jacksonMapper().build());
    }

    public JongoNative(MongoDatabase database, Mapper mapper) {
        this.mapper = mapper;
        this.database = database;
        this.codecRegistry = createCodecRegistry(mapper);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return this.database.getCollection(collectionName);
    }

    public <T> MongoCollection<T> getCollection(String collectionName, Class<T> documentClass) {
        MongoCollection<T> collection = this.database.getCollection(collectionName, documentClass);
        return wrapCollection(collection);
    }

    public BsonDocument query(String query, Object... parameters) {
        Query q = mapper.getQueryFactory().createQuery(query, parameters);
        return q.toBsonDocument();
    }

    public Bson id(Object id) {
        return query("{_id:#}", id);
    }

    private <T> com.mongodb.client.MongoCollection<T> wrapCollection(com.mongodb.client.MongoCollection<T> collection) {
        return collection.withCodecRegistry(codecRegistry);
    }

    private CodecRegistry createCodecRegistry(Mapper mapper) {

        CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        CodecRegistry jongoRegistry = CodecRegistries.fromProviders(new BsonValueCodecProvider(), new JongoCodecProvider(mapper));

        return CodecRegistries.fromRegistries(defaultRegistry, jongoRegistry);
    }
}
