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

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.mongodb.DBEncoder;
import com.mongodb.DBEncoderFactory;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBEncoder;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.OutputBuffer;
import org.jongo.bson.BsonDBEncoder;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.configuration.Mapping;

import java.util.Map;

import static org.jongo.bench.BenchUtil.asDBObject;
import static org.jongo.bench.BenchUtil.createFriend;

public class EncoderBench extends SimpleBenchmark {

    private final JacksonEngine engine = new JacksonEngine(Mapping.defaultMapping());
    private final DBApiLayerEmulator dbApiLayer = new DBApiLayerEmulator();

    public void timeEncodeWithDriver(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject dbo = asDBObject(createFriend(i));
            dbApiLayer.encode(DefaultDBEncoder.FACTORY, dbo);
        }
    }

    public void timeEncodeWithBsonJongo(int reps) {
        for (int i = 0; i < reps; i++) {
            DBObject friend = engine.marshall(createFriend(i)).toDBObject();
            dbApiLayer.encode(BsonDBEncoder.FACTORY, friend);
        }
    }

    private static class DBApiLayerEmulator {

        private byte[] encode(DBEncoderFactory factory, DBObject dbo) {
            Object id = dbo.get("_id");
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
                throw new IllegalArgumentException("projection stored in the db can't have . in them. (Bad Key: '" + s + "')");
            if (s.startsWith("$"))
                throw new IllegalArgumentException("projection stored in the db can't start with '$' (Bad Key: '" + s + "')");
        }

    }

    public static void main(String[] args) {
        Runner.main(EncoderBench.class, new String[]{});
    }
}
