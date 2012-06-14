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

import java.net.UnknownHostException;

import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.model.People;
import org.junit.rules.ExternalResource;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

public class JongoTest extends ExternalResource {

    private static Jongo jongo;
    public static MongoCollection collection;

    static {
        JacksonProcessor processor = new JacksonProcessor();
        prepareMarshallingStrategy(processor, processor);
    }

    public static void prepareMarshallingStrategy(Marshaller marshaller, Unmarshaller unmarshaller) {
        jongo = new Jongo(findDatabase(), marshaller, unmarshaller);
    }

    public static JongoTest collection(String collectionName) {
        return new JongoTest(collectionName);
    }

    private JongoTest(String collectionName) {
        collection = jongo.getCollection(collectionName);
        collection.drop();
    }

    @Override
    protected void after() {
        collection.drop();
    };

    public static final String MONGOHQ_FLAG = "jongo.mongohq.uri";

    private static DB findDatabase() {
        try {
            if (mustRunTestsAgainstMongoHQ())
                return getDBFromMongoHQ();
            else
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
        return new Mongo("127.0.0.1").getDB("jongo");
    }

    public DB getDatabase() {
        return jongo.getDatabase();
    }

    public static People newPeople() {
        return new People("John", "22 Wall Street Avenue");
    }
}
