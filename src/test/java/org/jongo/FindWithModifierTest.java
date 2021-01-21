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

import com.mongodb.DBCursor;
import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;


public class FindWithModifierTest extends JongoTestBase {

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
    public void canFindWithHint() throws Exception {
        /* given */
        Friend noName = new Friend(new ObjectId(), null);
        collection.save(noName);

        collection.ensureIndex("{name: 1}", "{sparse: true}");

        /* when */
        // force to use _id index instead of name index which is sparsed
        Iterator<Friend> friends = collection.find().hint("{$natural: 1}").sort("{name: 1}").as(Friend.class);

        /* then */
        assertThat(friends.hasNext()).isTrue();
    }

    @Test
    public void canUseQueryModifier() throws Exception {
        /* given */
        collection.save(new Friend(new ObjectId(), "John"));
        collection.save(new Friend(new ObjectId(), "Robert"));

        /* when */
        Iterator<Friend> friends = collection.find()
                .with(new QueryModifier() {
                    public void modify(DBCursor cursor) {
                        cursor.limit(1);
                    }
                })
                .as(Friend.class);

        /* then */
        assertThat(friends.hasNext()).isTrue();
        friends.next();
        assertThat(friends.hasNext()).isFalse();
    }

}
