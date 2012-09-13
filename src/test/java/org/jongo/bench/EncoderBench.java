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

import java.util.Map;

import static org.jongo.bench.BenchUtil.createDBOFriend;
import static org.jongo.bench.BenchUtil.createFriend;

public class EncoderBench extends SimpleBenchmark {

    private final StreamProcessor streamProcessor = new StreamProcessor();
    private final JacksonProcessor jacksonProcessor = new JacksonProcessor();
    private final DBApiLayerEmulator dbApiLayer = new DBApiLayerEmulator();


    public void timeEncodeWithDriver(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject dbo = createDBOFriend(i);
            dbApiLayer.encode(DefaultDBEncoder.FACTORY, dbo);
        }
    }

    public void timeEncodeWithDefaultJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject friend = jacksonProcessor.marshall(createFriend(i));
            dbApiLayer.encode(BeanEncoder.FACTORY, friend);
        }
    }

    public void timeEncodeWithStreamJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject friend = streamProcessor.marshall(createFriend(i));
            dbApiLayer.encode(BeanEncoder.FACTORY, friend);
        }
    }

    private static class DBApiLayerEmulator {

        private byte[] encode(DBEncoderFactory factory, DBObject dbo) {
            if (dbo.get("_id") == null || dbo.isPartialObject())
                throw new RuntimeException();

            _checkKeys(dbo);

            DBEncoder encoder = factory.create();
            OutputBuffer buffer = new BasicOutputBuffer();
            encoder.writeObject(buffer, dbo);
            return buffer.toByteArray();
        }

        private void _checkKeys(DBObject o) {
            for (String s : o.keySet()) {
                validateKey(s);
                Object inner = o.get(s);
                if (inner instanceof DBObject) {
                    _checkKeys((DBObject) inner);
                } else if (inner instanceof Map) {
                    _checkKeys((Map<String, Object>) inner);
                }
            }
        }

        private void _checkKeys(Map<String, Object> o) {
            for (String s : o.keySet()) {
                validateKey(s);
                Object inner = o.get(s);
                if (inner instanceof DBObject) {
                    _checkKeys((DBObject) inner);
                } else if (inner instanceof Map) {
                    _checkKeys((Map<String, Object>) inner);
                }
            }
        }

        private void validateKey(String s) {
            if (s.contains("."))
                throw new IllegalArgumentException("fields stored in the db can't have . in them. (Bad Key: '" + s + "')");
            if (s.startsWith("$"))
                throw new IllegalArgumentException("fields stored in the db can't start with '$' (Bad Key: '" + s + "')");
        }

    }

    public static void main(String[] args) {
        Runner.main(EncoderBench.class, new String[]{});
    }
}
