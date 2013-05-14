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

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;

class MongoHolder {

    private static final String MONGOHQ_FLAG = "jongo.mongohq.uri";

    public static MongoClient getInstance() {
        if (mustRunTestsAgainstMongoHQ()) {
            return MongoHQ.instance;
        }
        return LocalMongo.instance;
    }

    private static boolean mustRunTestsAgainstMongoHQ() {
        return System.getProperty(MONGOHQ_FLAG) != null;
    }

    private static class MongoHQ {

        private static MongoClient instance = getAuthenticatedInstance();

        private static MongoClient getAuthenticatedInstance() {
            try {
                String uri = System.getProperty(MONGOHQ_FLAG);
                MongoClientURI mongoURI = new MongoClientURI(uri);
                MongoClient mongo = new MongoClient(mongoURI);
				DB db = mongo.getDB(mongoURI.getDatabase());
                db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
                return mongo;
            } catch (UnknownHostException e) {
                throw new RuntimeException("Unable to reach mongo database test instance", e);
            }
        }
    }

    private static class LocalMongo {
    	
        private static MongoClient instance = getLocalInstance();

        private static MongoClient getLocalInstance() {
    	    try {
    			int port = RandomPortNumberGenerator.pickAvailableRandomEphemeralPortNumber();
    			EmbeddedMongo mongo = new EmbeddedMongo(port);
    			mongo.setWriteConcern(WriteConcern.FSYNC_SAFE);
    			return mongo;
    		} catch (Exception e) {
    			throw new RuntimeException("Failed to initialize Embedded Mongo instance: " + e, e);
    		}
        }
    }
}
