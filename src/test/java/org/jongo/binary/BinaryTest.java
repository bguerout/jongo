package org.jongo.binary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import lombok.EqualsAndHashCode;
import org.bson.types.Binary;
import org.jongo.MongoCollection;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BinaryTest extends JongoTestCase {

    @EqualsAndHashCode(of = {"data", "type"})
    private static class FriendId extends Binary {
        public FriendId(byte[] data) {
            super(data);
        }
    }

    @EqualsAndHashCode(of = {"id", "name"})
    private static class Friend {
        @JsonProperty("_id")
        private Binary id;
        private String name;

        public FriendId getId() {
            return new FriendId(id.getData());
        }

        public void setId(FriendId id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private Friend friend;
    private FriendId friendId;
    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        friend = new Friend();
        friendId = new FriendId("jongo".getBytes());
        friend.setId(friendId);
        friend.setName("jongo");

        collection = createEmptyCollection("friends");
        collection.getDBCollection().insert(
                new BasicDBObject(
                        "_id", friend.getId()).append(
                        "name", friend.getName()));

    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }


    @Test
    public void testQuery() throws Exception {
        Friend testFriend = collection.find().as(Friend.class).iterator().next();
        assertEquals(friend, testFriend);
    }


    @Test
    public void testSave() throws Exception {
        Friend expected = new Friend();
        FriendId expectedId = new FriendId("friend2".getBytes());
        expected.setId(expectedId);
        expected.setName("friend2");
        collection.save(expected);

        Friend actual = collection.findOne("{ _id: #}", expectedId).as(Friend.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testUpdate() throws Exception {
        friend.setName("new friend");
        collection.update("{ _id: #}", friendId).with("#", friend);

        Friend actual = collection.findOne("{ _id: #}", friendId).as(Friend.class);
        assertEquals(friend, actual);
    }

    @Test
    public void testInsert() throws Exception {
        Friend expected = new Friend();
        FriendId expectedId = new FriendId("friend2".getBytes());
        expected.setId(expectedId);
        expected.setName("friend2");
        collection.insert("#", expected);

        Friend actual = collection.findOne("{ _id: #}", expectedId).as(Friend.class);
        assertEquals(expected, actual);

    }

    @Test
    public void testRemove() throws Exception {
        collection.remove("{ _id: #}", friendId);

        Friend actual = collection.findOne("{ _id: #}", friendId).as(Friend.class);
        assertNull(actual);
    }
}
