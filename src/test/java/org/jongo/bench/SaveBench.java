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

import static org.jongo.bench.BenchUtil.*;

public class SaveBench extends SimpleBenchmark {

    @Param({"10"})
    int size;
    private MongoCollection mongoCollection;
    private DBCollection dbCollection;

    protected void setUp() throws Exception {
        mongoCollection = getCollectionFromJongo();
        dbCollection = getCollectionFromDriver();
        mongoCollection.drop();
        if (mongoCollection.count() > 0) {
            System.exit(1);
        }
    }

    public void timeSaveWithDriver(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < size; j++) {
                dbCollection.save(createDBOFriend(reps + j), WriteConcern.SAFE);
            }
        }
    }

    public void timeSaveWithJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < size; j++) {
                mongoCollection.save(createFriend(reps + j), WriteConcern.SAFE);
            }
        }
    }

    public static void main(String[] args) {
        Runner.main(SaveBench.class, new String[]{"-Dsize=1,10,100,1000"});
    }
}
