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

package org.jongo.bench;

import com.mongodb.*;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.BsonProcessor;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;

import java.net.UnknownHostException;

class BenchUtil {

    public static Friend createFriend(int id) {
        return new Friend("John" + id, "Address" + id, new Coordinate(1, id));
    }

    public static DBObject createDBOFriend(int id) {

        Friend friend = createFriend(id);

        DBObject dbo = new BasicDBObject();
        dbo.put("name", friend.getName());
        dbo.put("address", friend.getAddress());

        BasicDBObject coordinate = new BasicDBObject();
        coordinate.put("lat", friend.getCoordinate().lat);
        coordinate.put("lng", friend.getCoordinate().lng);

        dbo.put("coordinate", coordinate);

        return dbo;
    }

    public static DBCollection getCollectionFromDriver() throws UnknownHostException {
        Mongo nativeMongo = new Mongo();
        return nativeMongo.getDB("jongo").getCollection("benchmark");
    }

    public static MongoCollection getCollectionFromJongo() throws UnknownHostException {
        Mongo mongo = new Mongo();
        DB db = mongo.getDB("jongo");
        Jongo jongo = new Jongo(db);
        return jongo.getCollection("benchmark");
    }

    public static MongoCollection getStreamCollectionFromJongo() throws UnknownHostException {
        Mongo mongo = new Mongo();
        DB db = mongo.getDB("jongo");
        BsonProcessor bsonProcessor = new BsonProcessor();
        Jongo jongo = new Jongo(db, bsonProcessor, bsonProcessor);
        return jongo.getCollection("benchmark");
    }
}
