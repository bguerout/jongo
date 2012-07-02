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

package org.jongo;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;

public class FindWithResultMapperTest extends JongoTestCase {

    private DefaultMongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canFind() throws Exception {
        /* given */
        ResultMapper mapper = mock(ResultMapper.class);
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        for (Object o : collection.find().map(mapper)) {
            // use mapped object
        }

        /* then */
        verify(mapper).map(any(DBObject.class));
    }

    @Test
    public void canFindOne() throws Exception {
        /* given */
        ResultMapper mapper = mock(ResultMapper.class);
        Friend john = new Friend("John", "22 Wall Street Avenue");
        collection.save(john);

        /* when */
        collection.findOne().map(mapper);

        /* then */
        verify(mapper).map(any(DBObject.class));

    }
}
