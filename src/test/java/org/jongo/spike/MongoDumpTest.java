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

package org.jongo.spike;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.WriteConcern;
import de.undercouch.bson4jackson.BsonFactory;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.jongo.MongoCollection;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoDumpTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void importBsonDumpFileIntoCollection() throws Exception {

        InputStream bsonDump = getClass().getClassLoader().getResourceAsStream("1000friends.bson");
        BsonFactory bsonFactory = new BsonFactory();
        //bsonFactory.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH); // fails when enabled
        ObjectReader reader = new ObjectMapper(bsonFactory).reader(BasicBSONObject.class);

        MappingIterator<BSONObject> iterator = reader.readValues(bsonDump);
        try {
            while (iterator.hasNext()) {
                BSONObject bsonObject = iterator.next();
                collection.withWriteConcern(WriteConcern.MAJORITY).save(bsonObject);
            }
        } finally {
            iterator.close();
        }

        assertThat(collection.count()).isEqualTo(1000);
    }
}
