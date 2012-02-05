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
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonProcessor;

import java.net.UnknownHostException;

public class TestUtil {

    private static Database database = new Database();

    public static MongoCollection createEmptyCollection(String collectionName) throws UnknownHostException {
        MongoCollection col = getCollection(collectionName);
        col.drop();
        return col;
    }

    public static MongoCollection getCollection(String collectionName) throws UnknownHostException {
        DBCollection collection = getDatabase().getCollection(collectionName);
        return new MongoCollection(collection, new JacksonProcessor(), new JacksonProcessor());
    }

    public static DB getDatabase() throws UnknownHostException {

        return database.get();
    }


    public static void dropCollection(String collectionName) throws UnknownHostException {
        getDatabase().getCollection(collectionName).drop();
    }

}
