package org.jongo;

import com.mongodb.WriteResult;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class UpdateTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("users");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("users");
    }

    @Test
    public void canUpdateQuery() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("Peter"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}", "{$unset:{name:1}}");

        /* then */
        Iterator<People> peoples = collection.find("{name:{$exists:true}}").as(People.class).iterator();
        assertThat(peoples).hasSize(1);
        assertThat(writeResult).isNotNull();
    }
}
