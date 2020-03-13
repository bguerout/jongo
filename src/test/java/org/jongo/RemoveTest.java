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

import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoveTest extends JongoTestBase {

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
    public void canRemoveASpecificDocument() throws Exception {
        /* given */
        collection.save(new Friend("John"));
        collection.save(new Friend("Peter"));

        /* when */
        WriteResult writeResult = collection.remove("{name:'John'}");

        /* then */
        Iterable<Friend> friends = collection.find().as(Friend.class);
        assertThat(friends).hasSize(1);
        assertThat(writeResult).isNotNull();
    }

    @Test
    public void canRemoveByObjectId() throws Exception {
        /* given */
        collection.insert("{ _id:{$oid:'47cc67093475061e3d95369d'}, name:'John'}");

        /* when */
        ObjectId id = new ObjectId("47cc67093475061e3d95369d");
        WriteResult writeResult = collection.remove(id);

        /* then */
        assertThat(writeResult).isNotNull();
        Friend friend = collection.findOne().as(Friend.class);
        assertThat(friend).isNull();
    }

    @Test
    public void canRemoveWithParameters() throws Exception {
        /* given */
        collection.insert("{name:'John'}");

        /* when */
        WriteResult writeResult = collection.remove("{name:#}", "John");

        /* then */
        assertThat(writeResult).isNotNull();
        Friend friend = collection.findOne().as(Friend.class);
        assertThat(friend).isNull();
    }

    @Test
    public void canRemoveAll() throws Exception {
        /* given */
        collection.insert("{name:'John'}");
        collection.insert("{name:'Peter'}");

        /* when */
        WriteResult writeResult = collection.remove();

        /* then */
        assertThat(writeResult).isNotNull();
        Friend friend = collection.findOne().as(Friend.class);
        assertThat(friend).isNull();
    }
}
