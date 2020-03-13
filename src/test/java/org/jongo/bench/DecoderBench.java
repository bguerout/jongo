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
import com.mongodb.*;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDBDecoder;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;

public class DecoderBench extends SimpleBenchmark {

    private final JacksonEngine engine = new JacksonEngine(Mapping.defaultMapping());

    public void timeDecodeWithDriver(int reps) {

        for (int i = 0; i < reps; i++) {

            DBObject dbo = decode(DefaultDBDecoder.FACTORY);
            DBObject coord = (DBObject) dbo.get("coordinate");
            Coordinate coordinate = new Coordinate((Integer) coord.get("lat"), (Integer) coord.get("lng"));
            Friend f = new Friend((String) dbo.get("name"), (String) dbo.get("address"), coordinate);
        }
    }

    public void timeDecodeWithBsonJongo(int reps) {

        for (int docIndex = 0; docIndex < reps; docIndex++) {
            DBObject dbo = decode(BsonDBDecoder.FACTORY);
            BsonDocument document = Bson.createDocument(dbo);
            Friend f = engine.unmarshall(document, Friend.class);
        }
    }

    private DBObject decode(DBDecoderFactory factory) {
        DBDecoder decoder = factory.create();
        return decoder.decode(FRIEND_AS_BYTE, (DBCollection) null);
    }

    public static void main(String[] args) {
        Runner.main(DecoderBench.class, new String[]{});
    }

    private static final byte[] FRIEND_AS_BYTE = new byte[]{-75, 1, 0, 0, 7, 95, 105, 100, 0, 80, 116, -128, -1, 48, 4,
            -104, 62, 31, -27, -19, 85, 2, 110, 97, 109, 101, 0, 6, 0, 0, 0, 74, 111, 104, 110, 48, 0, 2, 97, 100, 100,
            114, 101, 115, 115, 0, 9, 0, 0, 0, 65, 100, 100, 114, 101, 115, 115, 48, 0, 3, 99, 111, 111, 114, 100, 105,
            110, 97, 116, 101, 0, 23, 0, 0, 0, 16, 108, 97, 116, 0, 1, 0, 0, 0, 16, 108, 110, 103, 0, 0, 0, 0, 0, 0, 4,
            98, 117, 100, 100, 105, 101, 115, 0, 77, 1, 0, 0, 3, 48, 0, 79, 0, 0, 0, 2, 110, 97, 109, 101, 0, 7, 0, 0, 0,
            74, 111, 104, 110, 48, 49, 0, 2, 97, 100, 100, 114, 101, 115, 115, 0, 9, 0, 0, 0, 65, 100, 100, 114, 101, 115,
            115, 48, 0, 3, 99, 111, 111, 114, 100, 105, 110, 97, 116, 101, 0, 23, 0, 0, 0, 16, 108, 97, 116, 0, 1, 0, 0, 0,
            16, 108, 110, 103, 0, 0, 0, 0, 0, 0, 0, 3, 49, 0, 79, 0, 0, 0, 2, 110, 97, 109, 101, 0, 7, 0, 0, 0, 74, 111, 104,
            110, 48, 50, 0, 2, 97, 100, 100, 114, 101, 115, 115, 0, 9, 0, 0, 0, 65, 100, 100, 114, 101, 115, 115, 48, 0, 3, 99,
            111, 111, 114, 100, 105, 110, 97, 116, 101, 0, 23, 0, 0, 0, 16, 108, 97, 116, 0, 1, 0, 0, 0, 16, 108, 110, 103, 0, 0,
            0, 0, 0, 0, 0, 3, 50, 0, 79, 0, 0, 0, 2, 110, 97, 109, 101, 0, 7, 0, 0, 0, 74, 111, 104, 110, 48, 51, 0, 2, 97, 100,
            100, 114, 101, 115, 115, 0, 9, 0, 0, 0, 65, 100, 100, 114, 101, 115, 115, 48, 0, 3, 99, 111, 111, 114, 100, 105, 110,
            97, 116, 101, 0, 23, 0, 0, 0, 16, 108, 97, 116, 0, 1, 0, 0, 0, 16, 108, 110, 103, 0, 0, 0, 0, 0, 0, 0, 3, 51, 0, 79,
            0, 0, 0, 2, 110, 97, 109, 101, 0, 7, 0, 0, 0, 74, 111, 104, 110, 48, 52, 0, 2, 97, 100, 100, 114, 101, 115, 115, 0, 9,
            0, 0, 0, 65, 100, 100, 114, 101, 115, 115, 48, 0, 3, 99, 111, 111, 114, 100, 105, 110, 97, 116, 101, 0, 23, 0, 0, 0, 16,
            108, 97, 116, 0, 1, 0, 0, 0, 16, 108, 110, 103, 0, 0, 0, 0, 0, 0, 0, 0, 0};
}
