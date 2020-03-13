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
import org.bson.types.ObjectId;
import org.jongo.marshall.MarshallingException;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.jongo.util.IdResultHandler;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class FindOneTest extends JongoTestBase {

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
    public void canFindOne() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend friend = collection.findOne("{name:'John'}").as(Friend.class);

        /* then */
        assertThat(friend.getName()).isEqualTo("John");
    }

    @Test
    public void canFindOneWithEmptyQuery() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend friend = collection.findOne().as(Friend.class);

        /* then */
        assertThat(friend.getName()).isEqualTo("John");
    }

    @Test
    public void canFindOneWithObjectId() throws Exception {
        /* given */
        Friend john = new Friend(new ObjectId(), "John");
        collection.save(john);

        Friend foundFriend = collection.findOne(john.getId()).as(Friend.class);

        /* then */
        assertThat(foundFriend).isNotNull();
        assertThat(foundFriend.getId()).isEqualTo(john.getId());
    }

    @Test
    public void canFindOneWithOid() throws Exception {
        /* given */
        ObjectId id = ObjectId.get();
        Friend john = new Friend(id, "John");
        collection.save(john);

        Friend foundFriend = collection.findOne("{_id:#}", id).as(Friend.class);

        /* then */
        assertThat(foundFriend).isNotNull();
        assertThat(foundFriend.getId()).isEqualTo(id);
    }

    @Test
    public void canFindOneWithOidAsString() throws Exception {
        /* given */
        ObjectId id = new ObjectId();
        Friend john = new Friend(id, "John");
        collection.save(john);

        Friend foundFriend = collection.findOne("{_id:{$oid:#}}", id.toString()).as(Friend.class);

        /* then */
        assertThat(foundFriend).isNotNull();
        assertThat(foundFriend.getId()).isEqualTo(id);
    }

    @Test
    public void shouldFailWhenUnableToUnmarshallResult() throws Exception {
        /* given */
        collection.insert("{error: 'NotaDate'}");

        /* when */
        try {
            collection.findOne().as(ErrorObject.class);
            fail();
        } catch (MarshallingException e) {
            assertThat(e.getMessage()).contains(" \"error\" : \"NotaDate\"");
        }
    }

    @Test
    public void whenNoResultShouldReturnNull() throws Exception {
        assertThat(collection.findOne("{_id:'invalid-id'}").as(Object.class)).isNull();
        assertThat(collection.findOne("{_id:'invalid-id'}").map(new IdResultHandler())).isNull();
        assertThat(collection.find("{_id:'invalid-id'}").as(Object.class).iterator()).hasSize(0);
    }

    @Test
    public void canFindOneWithReadPreference() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend friend = collection.withReadPreference(ReadPreference.primaryPreferred()).findOne("{name:'John'}").as(Friend.class);

        /* then */
        assertThat(friend.getName()).isEqualTo("John");

        // warning: we cannot check that ReadPreference is really used by driver, this unit test only checks the API
    }

    @Test
    public void canFindOneWithReadConcern() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend friend = collection.withReadConcern(ReadConcern.DEFAULT).findOne("{name:'John'}").as(Friend.class);

        /* then */
        assertThat(friend.getName()).isEqualTo("John");

        // warning: we cannot check that ReadConcern is really used by driver, this unit test only checks the API
    }


    @Test
    public void canOrderBy() throws Exception {

        collection.save(new Friend("John", "23 Wall Street Av."));
        collection.save(new Friend("John", "21 Wall Street Av."));
        collection.save(new Friend("John", "22 Wall Street Av."));

        Friend friend = collection.findOne().orderBy("{address:1}").as(Friend.class);

        assertThat(friend.getAddress()).isEqualTo("21 Wall Street Av.");
    }
}
