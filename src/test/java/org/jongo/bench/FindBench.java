/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.jongo.MongoCollection;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;

import static org.jongo.bench.BenchUtil.getCollectionFromDriver;
import static org.jongo.bench.BenchUtil.getCollectionFromJongo;
import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;

public class FindBench extends SimpleBenchmark {

    private static final int NB_DOCUMENTS = 100000;
    @Param({"1"})
    int size = 1;
    private MongoCollection bsonCollection;
    private DBCollection dbCollection;

    protected void setUp() throws Exception {
        bsonCollection = getCollectionFromJongo(jacksonMapper().build());
        dbCollection = getCollectionFromDriver();

        if (dbCollection.count() < NB_DOCUMENTS) {
            BenchUtil.injectFriendsIntoDB(NB_DOCUMENTS);
        }
    }

    public int timeDriverFind(int reps) {
        int insertions = 0;
        for (int i = 0; i < reps; i++) {
            DBCursor cursor = dbCollection.find().limit(size);
            for (DBObject dbo : cursor) {
                DBObject coord = (DBObject) dbo.get("coordinate");
                Coordinate coordinate = new Coordinate((Integer) coord.get("lat"), (Integer) coord.get("lng"));
                Friend f = new Friend((String) dbo.get("name"), (String) dbo.get("address"), coordinate);
                insertions++;
            }
        }
        return insertions;
    }

    public int timeJongoFind(int reps) {
        int insertions = 0;
        for (int i = 0; i < reps; i++) {
            for (Friend friend : bsonCollection.find().limit(size).as(Friend.class)) {
                insertions++;
            }
        }
        return insertions;
    }

    public static void main(String[] args) {
        Runner.main(FindBench.class, new String[]{
                //"--vm", "/opt/jvm/jdk1.6.0_37/bin/java,/opt/jvm/jdk1.7.0_10/bin/java",
                "-Dsize=1000,10000,50000,100000"
        });
    }
}
