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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.jongo.bson.BsonDBDecoder;
import org.jongo.bson.BsonDBEncoder;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.JsonProcessor;
import org.jongo.marshall.jackson.JacksonQueryBinder;
import org.jongo.marshall.jackson.configuration.MappingConfig;
import org.jongo.marshall.jackson.configuration.MappingConfigBuilder;
import org.jongo.query.QueryBinder;
import org.jongo.query.QueryFactory;

public class Jongo {

    private final DB database;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    private final QueryBinder queryBinder;

    public Jongo(DB database) {
        this.database = database;
        MappingConfig config = MappingConfigBuilder.usingJson().createConfiguration();
        JsonProcessor processor = new JsonProcessor(config);
        this.queryBinder = new JacksonQueryBinder(config);
        this.marshaller = processor;
        this.unmarshaller = processor;
    }

    public Jongo(DB database, Marshaller marshaller, Unmarshaller unmarshaller) {
        this.database = database;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        MappingConfig config = MappingConfigBuilder.usingJson().createConfiguration();
        this.queryBinder = new JacksonQueryBinder(config);
    }

    public MongoCollection getCollection(String name) {
        DBCollection dbCollection = database.getCollection(name);
        dbCollection.setDBDecoderFactory(BsonDBDecoder.FACTORY);
        dbCollection.setDBEncoderFactory(BsonDBEncoder.FACTORY);
        return new MongoCollection(dbCollection, marshaller, unmarshaller, new QueryFactory(queryBinder));
    }

    public DB getDatabase() {
        return database;
    }
}
