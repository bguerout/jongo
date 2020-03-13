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

package org.jongo.util;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.junit.BeforeClass;

import java.net.UnknownHostException;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;
import static org.junit.Assume.assumeTrue;

public abstract class JongoTestBase {

    private static MongoResource MONGO_RESOURCE;

    private Jongo jongo;
    private Mapper mapper;

    public JongoTestBase() {
        this(jacksonMapper().build());
    }

    protected JongoTestBase(Mapper mapper) {
        configure(mapper);
    }

    public void configure(Mapper mapper) {
        this.mapper = mapper;
        this.jongo = new Jongo(MONGO_RESOURCE.getDb("test_jongo"), mapper);
    }

    @BeforeClass
    public static void startMongo() throws Exception {
        MONGO_RESOURCE = new MongoResource();
    }

    protected MongoCollection createEmptyCollection(String collectionName) {
        MongoCollection col = jongo.getCollection(collectionName);
        col.drop();
        return col;
    }

    protected void dropCollection(String collectionName) {
        getDatabase().getCollection(collectionName).drop();
    }

    protected DB getDatabase() {
        return jongo.getDatabase();
    }

    protected Jongo getJongo() {
        return jongo;
    }

    protected Mapper getMapper() {
        return mapper;
    }

    protected void assumeThatMongoVersionIsGreaterThan(String expectedVersion) throws UnknownHostException {
        int expectedVersionAsInt = Integer.valueOf(expectedVersion.replaceAll("\\.", ""));
        CommandResult buildInfo = getDatabase().command("buildInfo");
        String version = (String) buildInfo.get("version");
        int currentVersion = Integer.valueOf(version.replaceAll("\\.", ""));
        assumeTrue(currentVersion >= expectedVersionAsInt);
    }
}
