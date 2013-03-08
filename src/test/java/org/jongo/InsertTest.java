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
import org.jongo.model.Coordinate;
import org.jongo.model.ExternalFriend;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class InsertTest extends JongoTestCase {

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
    public void canInsert() throws Exception {

        collection.insert("{name : 'Abby'}");

        assertThat(collection.count("{name : 'Abby'}")).isEqualTo(1);
    }

    @Test
    public void canInsertPojo() throws Exception {

        Friend friend = new Friend("John");

        collection.insert(friend);

        Friend result = collection.findOne("{name:'John'}").as(Friend.class);
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    public void canInsertPojos() throws Exception {

        Friend friend = new Friend("John");
        Friend friend2 = new Friend("Robert");

        collection.insert(friend, friend2);

        assertThat(collection.count("{name:'John'}")).isEqualTo(1);
        assertThat(collection.count("{name:'Robert'}")).isEqualTo(1);
    }

    @Test
    public void canInsertWithParameters() throws Exception {

        collection.insert("{name : #}", "Abby");

        assertThat(collection.count("{name : 'Abby'}")).isEqualTo(1);
    }

    @Test
    public void shouldInsertWithCollectionWriteConcern() throws Exception {

        WriteResult writeResult = collection.withConcern(WriteConcern.SAFE).insert("{name : 'Abby'}");

        assertThat(writeResult.getLastConcern()).isEqualTo(WriteConcern.SAFE);
    }

    @Test
    public void canInsertAnObjectWithoutId() throws Exception {

        Coordinate noId = new Coordinate(123, 1);

        collection.insert(noId);

        Coordinate result = collection.findOne().as(Coordinate.class);
        assertThat(result).isNotNull();
        assertThat(result.lat).isEqualTo(123);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotInsertAPojoWithId() throws Exception {

        ObjectId id = ObjectId.get();

        collection.withConcern(WriteConcern.SAFE).insert(new Friend(id, "John"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotInsertAPojoWithACustomId() throws Exception {

        collection.withConcern(WriteConcern.SAFE).insert(new ExternalFriend(122, "value"));
    }
}
