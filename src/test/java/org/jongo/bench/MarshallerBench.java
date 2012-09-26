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

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import org.jongo.marshall.jackson.BsonProcessor;
import org.jongo.marshall.jackson.JsonProcessor;

import static org.jongo.bench.BenchUtil.createFriend;

public class MarshallerBench extends SimpleBenchmark {

    private final BsonProcessor bsonProcessor = new BsonProcessor();
    private final JsonProcessor jsonProcessor = new JsonProcessor();

    public void timeMarshallAsDefault(int reps) {
        for (int i = 0; i < reps; i++) {
            jsonProcessor.marshall(createFriend(i));
        }
    }

    public void timeMarshallAsStream(int reps) {
        for (int i = 0; i < reps; i++) {
            bsonProcessor.marshall(createFriend(i));
        }
    }

    public void timeMarshallAsJson(int reps) {
        for (int i = 0; i < reps; i++) {
            jsonProcessor.marshall(createFriend(i));
        }
    }


    public static void main(String[] args) {
        Runner.main(MarshallerBench.class, new String[]{});
    }
}
