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
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonProcessor;

import java.net.UnknownHostException;

public class TestUtil {

    public static MongoCollection createEmptyCollection(String dbname, String collectionName) throws UnknownHostException {
        MongoCollection col = getCollection(dbname, collectionName);
        col.drop();
        return col;
    }

    public static MongoCollection getCollection(String dbname, String collectionName) throws UnknownHostException {
        DBCollection collection = getTestDatabase(dbname).getCollection(collectionName);
        return new MongoCollection(collection, new JacksonProcessor(), new JacksonProcessor());
    }

    public static DB getTestDatabase(String dbname) throws UnknownHostException {

        String mongoHQUri = System.getProperty("jongo.mongohq.uri");
        if (mongoHQUri != null) {
            return getDBFromMongoHQ(mongoHQUri);
        }
        return new Mongo("127.0.0.1").getDB(dbname);
    }

    private static DB getDBFromMongoHQ(String mongoHQUri) throws UnknownHostException {

        MongoURI mongoURI = new MongoURI(mongoHQUri);
        DB db = mongoURI.connectDB();
        db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
        return db;

    }

    public static void dropCollection(String dbname, String collectionName) throws UnknownHostException {
        getTestDatabase(dbname).getCollection(collectionName).drop();
    }
}
