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
import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBEncoder;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.OutputBuffer;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.marshall.stream.BeanEncoder;

import static org.jongo.bench.BenchUtil.createDBOFriend;
import static org.jongo.bench.BenchUtil.createFriend;

public class EncoderBench extends SimpleBenchmark {

    private JacksonProcessor processor;

    protected void setUp() throws Exception {
        processor = new JacksonProcessor();
    }

    public int timeEncodeWithDriver(int reps) {
        int i = 0;
        for (; i < reps; i++) {
            DBEncoder encoder = DefaultDBEncoder.FACTORY.create();
            OutputBuffer buffer = new BasicOutputBuffer();
            DBObject dbo = createDBOFriend(i);

            encoder.writeObject(buffer, dbo);

            byte[] bytes = buffer.toByteArray();
        }
        return i;
    }

    public int timeEncodeWithJongo(int reps) {
        int i = 0;
        for (; i < reps; i++) {
            DBObject friend = processor.marshall(createFriend(i));
            OutputBuffer buffer = new BasicOutputBuffer();
            BeanEncoder.FACTORY.create().writeObject(buffer, friend);
            byte[] bytes = buffer.toByteArray();
        }
        return i;
    }

    public static void main(String[] args) {
        Runner.main(EncoderBench.class, new String[]{});
    }
}
