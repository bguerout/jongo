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
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBDecoder;
import org.jongo.marshall.jackson.StreamProcessor;
import org.jongo.marshall.stream.BeanDecoder;
import org.jongo.marshall.stream.BsonStream;
import org.jongo.marshall.stream.BsonStreamFactory;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;

import static org.jongo.bench.BenchUtil.createDBOFriend;

public class DecoderBench extends SimpleBenchmark {

    private static final int NB_DOCS = 1000000;

    private StreamProcessor processor;
    private byte[][] documents = new byte[NB_DOCS][];

    @Override
    protected void setUp() throws Exception {
        processor = new StreamProcessor();
        for (int i = 0; i < NB_DOCS; i++) {
            DBObject dbObject = createDBOFriend(i);
            BsonStream stream = BsonStreamFactory.fromDBObject(dbObject);
            documents[i] = stream.getData();
        }
    }

    public void timeDecodeWithDriver(int reps) {
        for (int i = 0; i < reps; i++) {

            DBDecoder decoder = DefaultDBDecoder.FACTORY.create();
            DBObject dbo = decoder.decode(documents[i], (DBCollection) null);

            DBObject coord = (DBObject) dbo.get("coordinate");
            Coordinate coordinate = new Coordinate((Integer) coord.get("lat"), (Integer) coord.get("lng"));
            Friend f = new Friend((String) dbo.get("name"), (String) dbo.get("address"), coordinate);
        }
    }

    public void timeDecodeWithJongo(int reps) {
        for (int i = 0; i < reps; i++) {

            DBDecoder decoder = BeanDecoder.FACTORY.create();
            DBObject dbo = decoder.decode(documents[i], (DBCollection) null);

            Friend f = processor.unmarshall(dbo, Friend.class);
        }
    }


    public static void main(String[] args) {
        Runner.main(DecoderBench.class, new String[]{});
    }
}
