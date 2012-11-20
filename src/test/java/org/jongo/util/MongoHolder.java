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

import java.net.UnknownHostException;

class MongoHolder {

    private static final String MONGOHQ_FLAG = "jongo.mongohq.uri";
    private static volatile Mongo MONGO;

    public static Mongo getInstance() throws UnknownHostException {
        if (MONGO == null) {
            setInstance();
        }
        return MONGO;
    }

    private synchronized static void setInstance() throws UnknownHostException {
        if (mustRunTestsAgainstMongoHQ()) {
            MONGO = getMongoHQ();
        } else {
            MONGO = getLocalMongo();
        }
    }

    private static boolean mustRunTestsAgainstMongoHQ() {
        return System.getProperty(MONGOHQ_FLAG) != null;
    }

    private static Mongo getMongoHQ() throws UnknownHostException {
        String uri = System.getProperty(MONGOHQ_FLAG);
        MongoURI mongoURI = new MongoURI(uri);
        DB db = mongoURI.connectDB();
        db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
        return db.getMongo();
    }

    private static Mongo getLocalMongo() throws UnknownHostException {
        return new Mongo("127.0.0.1");
    }
}
