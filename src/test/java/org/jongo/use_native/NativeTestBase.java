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

package org.jongo.use_native;

import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.jongo.Jongo;
import org.jongo.JongoNative;
import org.jongo.Mapper;
import org.jongo.util.MongoResource;
import org.junit.BeforeClass;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public abstract class NativeTestBase {

    private static MongoResource MONGO_RESOURCE;

    protected JongoNative jongo;

    public NativeTestBase() {
        this(jacksonMapper().build());
    }

    protected NativeTestBase(Mapper mapper) {
        MongoDatabase database = MONGO_RESOURCE.getDatabase("test_jongo");
        this.jongo = Jongo.useNative(database, mapper);
    }

    @BeforeClass
    public static void startMongo() throws Exception {
        MONGO_RESOURCE = new MongoResource();
    }

    protected BsonDocument q(String query, Object... parameters) {
        return jongo.query(query, parameters);
    }

    protected Bson id(Object id) {
        return jongo.id(id);
    }
}
