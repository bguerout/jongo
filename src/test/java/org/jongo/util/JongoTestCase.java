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

package org.jongo.util;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;

import java.net.UnknownHostException;

import static org.junit.Assume.assumeTrue;

public abstract class JongoTestCase {

    private Jongo jongo;

    public JongoTestCase() {
        this.jongo = new Jongo(findDatabase(), new JacksonMapper.Builder().build());
    }

    protected MongoCollection createEmptyCollection(String collectionName) throws UnknownHostException {
        MongoCollection col = jongo.getCollection(collectionName);
        col.drop();
        return col;
    }

    protected void dropCollection(String collectionName) throws UnknownHostException {
        getDatabase().getCollection(collectionName).drop();
    }

    protected DB getDatabase() throws UnknownHostException {
        return jongo.getDatabase();
    }

    protected Jongo getJongo() {
        return jongo;
    }

    protected void assumeThatMongoVersionIsGreaterThan(String expectedVersion) throws UnknownHostException {
        int expectedVersionAsInt = Integer.valueOf(expectedVersion.replaceAll("\\.", ""));
        CommandResult buildInfo = getDatabase().command("buildInfo");
        String version = (String) buildInfo.get("version");
        int currentVersion = Integer.valueOf(version.replaceAll("\\.", ""));
        assumeTrue(currentVersion >= expectedVersionAsInt);
    }

    public void prepareMarshallingStrategy(Mapper mapper) {
        this.jongo = new Jongo(findDatabase(), mapper);
    }

    private static DB findDatabase() {
        try {
            return MongoHolder.getInstance().getDB("jongo");
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to reach mongo database test instance", e);
        }
    }
}
