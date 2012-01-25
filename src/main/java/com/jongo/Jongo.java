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

package com.jongo;

import com.jongo.marshall.Marshaller;
import com.jongo.marshall.Unmarshaller;
import com.jongo.marshall.jackson.JacksonProcessor;
import com.mongodb.*;

import java.net.UnknownHostException;

import static com.jongo.MongoCollection.MONGO_ID;

public class Jongo {

    private final DB database;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    private Jongo(DB database, Marshaller marshaller, Unmarshaller unmarshaller) {
        this.database = database;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }
    
    public static Jongo jongo(String dbname) throws UnknownHostException {
        return new Builder(dbname).build();
    }

    public static class Builder {
        private final DB database;

        private Marshaller marshaller;
        private Unmarshaller unmarshaller;

        public Builder(DB database) {
            this.database = database;

            JacksonProcessor jacksonProcessor = new JacksonProcessor();
            marshaller = jacksonProcessor;
            unmarshaller = jacksonProcessor;
        }

        public Builder(String dbname) throws UnknownHostException {
            this(new Mongo().getDB(dbname));
        }

        public Builder marshaller(Marshaller marshaller) {
            this.marshaller = marshaller;
            return this;
        }

        public Builder unmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
            return this;
        }

        public Jongo build() {
            return new Jongo(database, marshaller, unmarshaller);
        }
    }

    public MongoCollection getCollection(String name) {
        DBCollection dbCollection = database.getCollection(name);
        return new MongoCollection(dbCollection, marshaller, unmarshaller);
    }

    public DB getDatabase() {
        return database;
    }

    static String toJson(DBObject dbObject) {
        Object id = dbObject.get(MONGO_ID);
        if (id != null)
            dbObject.put(MONGO_ID, id.toString());

        return dbObject.toString();
    }
}
