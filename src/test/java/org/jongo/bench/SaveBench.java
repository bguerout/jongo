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

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;

import static org.jongo.bench.BenchUtil.*;

public class SaveBench extends SimpleBenchmark {

    @Param({"1"})
    int size = 1;

    private DBCollection dbCollection;
    private MongoCollection bsonCollection;

    protected void setUp() throws Exception {

        bsonCollection = getCollectionFromJongo(new JacksonMapper.Builder().build()).withConcern(WriteConcern.SAFE);
        dbCollection = getCollectionFromDriver();

        bsonCollection.drop();
    }

    public void timeDriverSave(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < size; j++) {
                dbCollection.save(asDBObject(createFriend(reps + j)));
            }
        }
    }

    public void timeJongoSave(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < size; j++) {
                bsonCollection.save(createFriend(reps + j));
            }
        }
    }

    public static void main(String[] args) {
        Runner.main(SaveBench.class, new String[]{
                //"--vm", "/opt/jvm/jdk1.6.0_37/bin/java,/opt/jvm/jdk1.7.0_10/bin/java",
                "-Dsize=1000,10000,50000"
        });
    }
}
