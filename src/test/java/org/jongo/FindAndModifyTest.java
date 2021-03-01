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
import org.bson.types.ObjectId;
import org.jongo.marshall.MarshallingException;
import org.jongo.model.ExposableFriend;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FindAndModifyTest extends JongoTestBase {

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
    public void canFindAndModifyOne() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend originalFriend = collection.findAndModify("{name:#}", "John").with("{$set: {address: #}}", "A better place").as(Friend.class);

        /* then */
        assertThat(originalFriend.getAddress()).isEqualTo("22 Wall Street Avenue");

        Friend updatedFriend = collection.findOne().as(Friend.class);
        assertThat(updatedFriend.getAddress()).isEqualTo("A better place");
        assertThat(updatedFriend.getName()).isEqualTo("John");
    }

    @Test
    public void canFindAndModifyWithNullValue() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend originalFriend = collection.findAndModify("{name:#}", "John").with("{$set: {address: #}}", null).as(Friend.class);

        /* then */
        assertThat(originalFriend.getAddress()).isEqualTo("22 Wall Street Avenue");

        Friend updatedFriend = collection.findOne().as(Friend.class);
        assertThat(updatedFriend.getAddress()).isNull();
    }

    @Test
    public void canFindAndModifyWithResultHandler() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        DBObject dbo = collection.findAndModify("{name:#}", "John").with("{$set: {address: #}}", "A better place").map(new RawResultHandler<DBObject>());

        /* then */
        assertThat(dbo.get("name")).isEqualTo("John");
    }

    @Test
    public void canReturnNew() throws Exception {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend updatedFriend = collection.findAndModify().with("{$set: {address: 'A better place'}}").returnNew().as(Friend.class);

        /* then */
        assertThat(updatedFriend.getAddress()).isEqualTo("A better place");
    }

    @Test
    public void canRemove() {
        /* given */
        collection.save(new Friend("John", "22 Wall Street Avenue"));

        /* when */
        Friend deletedFriend = collection.findAndModify().remove().as(Friend.class);

        /* then */
        assertThat(deletedFriend.getName()).isEqualTo("John");
        assertThat(collection.count()).isEqualTo(0);
    }

    @Test
    public void canSort() {
        /* given */
        collection.save(new Friend("John", "22 Wall Streem Avenue"));
        collection.save(new Friend("Wally", "22 Wall Streem Avenue"));

        /* when */
        Friend friend = collection.findAndModify()
                .sort("{name: -1}")
                .with("{$set: {address:'Sesame Street'}}")
                .as(Friend.class);

        /* then */
        assertThat(friend.getName()).isEqualTo("Wally");
    }

    @Test
    public void shouldFailWhenUnableToUnmarshallResult() throws Exception {
        /* given */
        collection.insert("{error: 'NotaDate'}");

        /* when */
        try {
            collection.findAndModify("{error: 'NotaDate'}").with("{$set: {error: 'StillNotaDate'}}").as(ErrorObject.class);
            fail();
        } catch (MarshallingException e) {
            assertThat(e.getMessage()).contains("\"error\": \"NotaDate\"");
        }
    }

    @Test
    public void canFindWithProjection() throws Exception {

        collection.save(new Friend("John", "Jermin Street"));

        collection.findAndModify("{name:'John'}").with("{$set: {name:'Robert'}}").projection("{name:1}").map(new ResultHandler<Boolean>() {
            public Boolean map(DBObject result) {
                assertThat(result.containsField("name")).isTrue();
                assertThat(result.containsField("address")).isFalse();
                return true;
            }
        });
    }

    @Test
    public void canFindWithProjectionParams() throws Exception {

        collection.save(new Friend("John", "Jermin Street"));

        collection.findAndModify("{name:'John'}").with("{$set: {name:'Robert'}}").projection("{name:#}", 1).map(new ResultHandler<Boolean>() {
            public Boolean map(DBObject result) {
                assertThat(result.containsField("name")).isTrue();
                assertThat(result.containsField("address")).isFalse();
                return true;
            }
        });
    }

    @Test
    public void canUpsertByObjectId() throws Exception {
        Friend expected = new Friend(new ObjectId(), "John");
        Friend actual = collection.findAndModify("{_id: #}", expected.getId())
                .upsert()
                .returnNew()
                .with("{$setOnInsert: {name: #}}", "John")
                .as(Friend.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void canUpsertByStringId() throws Exception {
        ExposableFriend expected = new ExposableFriend(new ObjectId().toString(), "John");
        ExposableFriend actual = collection.findAndModify("{_id: #}", expected.getId())
                .upsert()
                .returnNew()
                .with("{$setOnInsert: {name: #}}", "John")
                .as(ExposableFriend.class);

        assertThat(actual).isEqualTo(expected);
    }

}
