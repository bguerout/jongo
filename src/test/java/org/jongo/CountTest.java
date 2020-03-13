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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CountTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    public Friend newFriend() {
        return new Friend("John", "22 Wall Street Avenue");
    }

    @Test
    public void canCount() throws Exception {
        /* given */
        collection.save(newFriend());
        collection.save(newFriend());

        /* then */
        assertThat(collection.count()).isEqualTo(2);
    }

    @Test
    public void canCountWithQuery() throws Exception {
        /* given */
        collection.save(newFriend());
        collection.save(newFriend());

        /* then */
        assertThat(collection.count("{name:{$exists:true}}")).isEqualTo(2);
    }

    @Test
    public void canCountWithParameters() throws Exception {
        /* given */
        collection.save(newFriend());
        collection.save(new Friend("Peter", "22 Wall Street Avenue"));

        /* then */
        assertThat(collection.count("{name:#}", "Peter")).isEqualTo(1);
    }

    @Test
    public void canCountWithReadPreference() throws Exception {
        /* given */
        collection.save(newFriend());
        collection.save(newFriend());

        /* then */
        assertThat(collection.withReadPreference(ReadPreference.primaryPreferred()).count()).isEqualTo(2);

        // warning: we cannot check that ReadPreference is really used by driver, this unit test only checks the API
    }

    @Test
    public void canCountWithReadConcern() throws Exception {
        /* given */
        collection.save(newFriend());
        collection.save(newFriend());

        /* then */
        assertThat(collection.withReadConcern(ReadConcern.DEFAULT).count()).isEqualTo(2);

        // warning: we cannot check that ReadConcern is really used by driver, this unit test only checks the API
    }
}
