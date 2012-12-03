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

import static org.jongo.bench.BenchUtil.createDBOFriend;
import static org.jongo.bench.BenchUtil.createFriend;
import static org.jongo.bench.BenchUtil.getCollectionFromDriver;
import static org.jongo.bench.BenchUtil.getCollectionFromJongo;

import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;

public class SaveBench extends SimpleBenchmark {

    @Param({"1"})
    int size = 1;
    @Param("NORMAL")
    WriteConcern concern = WriteConcern.NORMAL;

    private DBCollection dbCollection;
    private MongoCollection bsonCollection;

    protected void setUp() throws Exception {

        bsonCollection = getCollectionFromJongo(new JacksonMapper.Builder().build());
        dbCollection = getCollectionFromDriver();

        bsonCollection.drop();
    }

    public void timeSaveWithDriver(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < size; j++) {
                dbCollection.save(createDBOFriend(reps + j), concern);
            }
        }
    }

    public void timeSaveWithBsonJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < size; j++) {
                bsonCollection.save(createFriend(reps + j), concern);
            }
        }
    }

    public static void main(String[] args) {
        Runner.main(SaveBench.class, new String[]{"-Dsize=1000,10000,50000", "-Dconcern=SAFE"});
    }
}
