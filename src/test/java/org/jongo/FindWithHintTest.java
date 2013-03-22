package org.jongo;

import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;


public class FindWithHintTest extends JongoTestCase {


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
        Friend friend = new Friend(new ObjectId(), "John");
        collection.save(friend);

        /* when */
        Iterator<Friend> friends = collection.find("{name:'John'}").hint("{$natural: 1}").as(Friend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isTrue();
        assertThat(friends.next().getName()).isEqualTo("John");
        assertThat(friends.hasNext()).isFalse();
    }

}
