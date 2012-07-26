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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.mongodb.DBObject;
import de.undercouch.bson4jackson.BsonFactory;

import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.marshall.jackson.json.JsonProcessor;
import org.jongo.marshall.stream.DocumentStream;
import org.jongo.marshall.stream.DocumentStreamFactory;
import org.jongo.model.Friend;

import static org.jongo.bench.BenchUtil.createDBOFriend;
import static org.jongo.bench.BenchUtil.createFriend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MarshallerBench extends SimpleBenchmark {

    private JacksonProcessor processor;
    private JacksonProcessor jsonProcessor;

    protected void setUp() throws Exception {
        processor = new JacksonProcessor();
        jsonProcessor = new JsonProcessor();
    }

    public void timeMarshall(int reps) {
        for (int i = 0; i < reps; i++) {
            processor.marshall(createFriend(i));
        }
    }

    public void timeMarshallWithJongo0_2(int reps) {
        for (int i = 0; i < reps; i++) {
            jsonProcessor.marshall(createFriend(i));
        }
    }


    public static void main(String[] args) {
        Runner.main(MarshallerBench.class, new String[]{});
    }
}
