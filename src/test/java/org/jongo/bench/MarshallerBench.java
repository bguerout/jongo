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

import static org.jongo.bench.BenchUtil.createFriend;

import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.marshall.jackson.ObjectMapperFactory;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.mongodb.DBObject;

public class MarshallerBench extends SimpleBenchmark {

    private JacksonProcessor processor;
    private JacksonProcessor jsonProcessor;
    private JacksonProcessor customProcessor;

    protected void setUp() throws Exception {
        processor = new JacksonProcessor();
        jsonProcessor = new JsonProcessor();

        JsonFactory bsonFactory = BsonModule.createFactory();
        ObjectMapper mapper = ObjectMapperFactory.createMapper(bsonFactory);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance());
        customProcessor = new JsonProcessor();
    }

    public int timeMarshall(int reps) {
        int i = 0;
        for (; i < reps; i++) {
            DBObject friend = processor.marshall(createFriend(i));
        }
        return i;
    }

    public int timeMarshallWithVisibilty(int reps) {
        int i = 0;
        for (; i < reps; i++) {
            DBObject friend = customProcessor.marshall(createFriend(i));
        }
        return i;
    }

    public int timeMarshallWithJongo0_2(int reps) {
        int i = 0;
        for (; i < reps; i++) {
            DBObject friend = jsonProcessor.marshall(createFriend(i));
        }
        return i;
    }


    public static void main(String[] args) {
        Runner.main(MarshallerBench.class, new String[]{});
    }
}
