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

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

public class UpdateTest extends JongoTestCase {

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
    public void canUpdateMultiDeprecated() throws Exception {
        /* given */
        collection.save(new Friend("John"));
        collection.save(new Friend("John"));

        try {
            collection.update("{name:'John'}", "{$unset:{name:1}}");
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("Beware 'update(String query, String modifier)' has been replaced");
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void canUpdateMulti() throws Exception {
        /* given */
        collection.save(new Friend("John"));
        collection.save(new Friend("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}").multi().with("{$unset:{name:1}}");

        /* then */
        Iterable<Friend> friends = collection.find("{name:{$exists:true}}").as(Friend.class);
        assertThat(friends).hasSize(0);
        assertThat(writeResult.getLastConcern()).isEqualTo(collection.getDBCollection().getWriteConcern());
    }

    @Test
    public void canUpdateMultiWithWriteConcern() throws Exception {
        /* given */
        collection.save(new Friend("John"));
        collection.save(new Friend("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}").multi().concern(WriteConcern.SAFE).with("{$unset:{name:1}}");

        /* then */
        Iterable<Friend> friends = collection.find("{name:{$exists:true}}").as(Friend.class);
        assertThat(friends).hasSize(0);
        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);

    }

    @Test
    public void canUpdateByObjectId() throws Exception {

        Friend friend = new Friend();
        collection.save(friend);

        /* when */
        collection.update(friend.getId()).with("{$set:{name:'John'}}");

        /* then */
        Friend john = collection.findOne("{name:'John'}").as(Friend.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(friend.getId()).isEqualTo(john.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenUpdateWithNullObjectId() throws Exception {

        collection.update((ObjectId) null);
    }

    @Test
    public void canUpsert() throws Exception {

        /* when */
        WriteResult writeResult = collection.update("{}").upsert().with("{$set:{name:'John'}}");

        /* then */
        Friend john = collection.findOne("{name:'John'}").as(Friend.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
    }

    @Test
    public void canUpsertWithWriteConcern() throws Exception {

        /* when */
        WriteResult writeResult = collection.update("{}").upsert().concern(WriteConcern.SAFE).with("{$set:{name:'John'}}");

        /* then */
        Friend john = collection.findOne("{name:'John'}").as(Friend.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }
}
