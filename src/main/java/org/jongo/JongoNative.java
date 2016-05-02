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
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.jongo.query.Query;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class JongoNative {

    private final Mapper mapper;
    private final MongoDatabase mongoDatabase;
    private final CodecRegistry codecRegistry;

    public JongoNative(MongoDatabase mongoDatabase) {
        this(mongoDatabase, jacksonMapper().build());
    }

    public JongoNative(MongoDatabase mongoDatabase, Mapper mapper) {
        this.mapper = mapper;
        this.mongoDatabase = mongoDatabase;
        this.codecRegistry = createCodecRegistry(mapper);
    }

    public Bson query(String query, Object... parameters) {
        Query q = mapper.getQueryFactory().createQuery(query, parameters);
        return q.toBson();
    }

    public Bson id(Object id) {
        return query("{_id:#}", id);
    }

    public <T> com.mongodb.client.MongoCollection<T> getCollection(String name, Class<T> clazz) {
        return mongoDatabase.getCollection(name, clazz).withCodecRegistry(codecRegistry);
    }

    public com.mongodb.client.MongoCollection<Bson> getCollection(String name) {
        return mongoDatabase.getCollection(name).withCodecRegistry(codecRegistry).withDocumentClass(Bson.class);
    }

    private static CodecRegistry createCodecRegistry(Mapper mapper) {

        CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        CodecRegistry jongoRegistry = CodecRegistries.fromProviders(new BsonValueCodecProvider(), new JongoCodecProvider(mapper));

        return CodecRegistries.fromRegistries(defaultRegistry, jongoRegistry);
    }
}
