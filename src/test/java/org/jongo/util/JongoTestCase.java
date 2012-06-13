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

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonProcessor;

import java.net.UnknownHostException;

public abstract class JongoTestCase {

    public static final String MONGOHQ_FLAG = "jongo.mongohq.uri";

    private TestContext testContext;
    private DB db;

    public JongoTestCase(TestContext testContext) {
        this.testContext = testContext;
        db = resolveDB();
    }

    public JongoTestCase() {
        JacksonProcessor processor = new JacksonProcessor();
        this.testContext = new TestContext(processor, processor);
        this.db = resolveDB();
    }

    protected MongoCollection createEmptyCollection(String collectionName) throws UnknownHostException {
        Jongo jongo = new Jongo(db, testContext.getMarshaller(), testContext.getUnmarshaller());
        MongoCollection col = jongo.getCollection(collectionName);
        col.drop();
        return col;
    }

    protected void dropCollection(String collectionName) throws UnknownHostException {
        getDB().getCollection(collectionName).drop();
    }

    protected DB getDB() throws UnknownHostException {
        return db;
    }

    private DB resolveDB() {
        try {

            if (mustRunTestsAgainstMongoHQ()) {
                return getDBFromMongoHQ();
            }
            return getLocalDB();

        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to reach mongo database test instance", e);
        }
    }

    private boolean mustRunTestsAgainstMongoHQ() {
        return System.getProperty(MONGOHQ_FLAG) != null;
    }

    private DB getDBFromMongoHQ() throws UnknownHostException {
        String mongoHQUri = System.getProperty(MONGOHQ_FLAG);
        MongoURI mongoURI = new MongoURI(mongoHQUri);
        DB db = mongoURI.connectDB();
        db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
        return db;
    }

    private DB getLocalDB() throws UnknownHostException {
        return new Mongo("127.0.0.1").getDB("jongo");
    }
}
