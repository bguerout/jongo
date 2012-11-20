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
import org.jongo.Provider;
import org.jongo.marshall.jackson.BsonProvider;

import java.net.UnknownHostException;

public abstract class JongoTestCase {

    public static final String MONGOHQ_FLAG = "jongo.mongohq.uri";

    private Jongo jongo;
    private static Mongo mongo;
    
    public JongoTestCase() {
        this.jongo = new Jongo(findDatabase(), new BsonProvider());
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

    private static DB findDatabase() {
        try {

            if (mustRunTestsAgainstMongoHQ()) {
                return getDBFromMongoHQ();
            }
            return getLocalDB();

        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to reach mongo database test instance", e);
        }
    }

    private static boolean mustRunTestsAgainstMongoHQ() {
        return System.getProperty(MONGOHQ_FLAG) != null;
    }

    private static DB getDBFromMongoHQ() throws UnknownHostException {
        String uri = System.getProperty(MONGOHQ_FLAG);
        MongoURI mongoURI = new MongoURI(uri);
        DB db = mongoURI.connectDB();
        db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
        return db;
    }

    private static DB getLocalDB() throws UnknownHostException {
    	if(mongo == null) {
    		mongo = new Mongo("127.0.0.1");
    	}
    	return mongo.getDB("jongo");
    }

    public void prepareMarshallingStrategy(Provider provider) {
        this.jongo = new Jongo(findDatabase(), provider);
    }

}
