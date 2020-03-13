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

package org.jongo;

import com.mongodb.DBObject;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FindWithResultMapperTest extends JongoTestBase {

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
    public void canFindAndMap() throws Exception {
        /* given */
        ResultHandler<DBObject> handler = new RawResultHandler<DBObject>();
        collection.save(new Friend("John", "22 Wall Street Avenue"));
        collection.save(new Friend("Peter", "22 Wall Street Avenue"));

        /* when */
        for (DBObject result : collection.find().map(handler)) {
            /* then */
            assertThat(result.get("name")).isIn("John", "Peter");
        }
    }

    @Test
    public void canFindOneAndMap() throws Exception {
        /* given */
        ResultHandler<DBObject> handler = new RawResultHandler<DBObject>();
        Friend john = new Friend("John", "22 Wall Street Avenue");
        collection.save(john);

        /* when */
        DBObject result = collection.findOne().map(handler);

        /* then */
        assertThat(result.get("name")).isEqualTo("John");


    }
}
