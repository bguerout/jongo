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
import com.mongodb.DBEncoderFactory;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBEncoder;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.OutputBuffer;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.marshall.jackson.StreamProcessor;
import org.jongo.marshall.stream.BeanEncoder;

import static org.jongo.bench.BenchUtil.createDBOFriend;
import static org.jongo.bench.BenchUtil.createFriend;

public class EncoderBench extends SimpleBenchmark {

    private final StreamProcessor streamProcessor = new StreamProcessor();
    private final JacksonProcessor jacksonProcessor = new JacksonProcessor();

    public void timeEncodeWithDriver(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject dbo = createDBOFriend(i);
            encode(dbo, DefaultDBEncoder.FACTORY);
        }
    }

    public void timeEncodeWithDefaultJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject friend = jacksonProcessor.marshall(createFriend(i));
            encode(friend, BeanEncoder.FACTORY);
        }
    }

    public void timeEncodeWithStreamJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject friend = streamProcessor.marshall(createFriend(i));
            encode(friend, BeanEncoder.FACTORY);
        }
    }

    private byte[] encode(DBObject friend, DBEncoderFactory factory) {
        DBEncoder encoder = factory.create();
        OutputBuffer buffer = new BasicOutputBuffer();
        encoder.writeObject(buffer, friend);
        return buffer.toByteArray();
    }

    public static void main(String[] args) {
        Runner.main(EncoderBench.class, new String[]{});
    }
}
