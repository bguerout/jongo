package org.jongo;

import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestCase;
import org.jongo.util.MongoInMemoryRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;


public class FindWithHintTest extends JongoTestCase {


    @Rule
    public static final MongoInMemoryRule mongo = new MongoInMemoryRule();

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
        Friend john = new Friend(new ObjectId(), "John");
        collection.save(john);

        Friend noName = new Friend(new ObjectId(), null);
        collection.save(noName);

        collection.ensureIndex("{name: 1}", "{sparse: true}");

        /* when */
        Iterator<Friend> friends = collection.find("{name: null}").hint("{name: 1}").as(Friend.class).iterator();

        /* then */
        assertThat(friends.hasNext()).isFalse();
    }

}
