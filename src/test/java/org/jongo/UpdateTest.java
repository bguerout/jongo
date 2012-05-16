package org.jongo;

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.jongo.model.People;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
    public void canUpdate() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}", "{$unset:{name:1}}");

        /* then */
        Iterator<People> peoples = collection.find("{name:{$exists:true}}").as(People.class).iterator();
        assertThat(peoples).hasSize(0);
        assertThat(writeResult).isNotNull();
    }

    @Test
    public void canUpdateWithWriteConcern() throws Exception {
        /* given */
        collection.save(new People("John"));
        collection.save(new People("John"));
        WriteConcern writeConcern = spy(WriteConcern.SAFE);

        /* when */
        WriteResult writeResult = collection.update("{name:'John'}", "{$unset:{name:1}}", writeConcern);

        /* then */
        Iterator<People> peoples = collection.find("{name:{$exists:true}}").as(People.class).iterator();
        assertThat(peoples).hasSize(0);
        assertThat(writeResult).isNotNull();
        verify(writeConcern).callGetLastError();
    }

    @Test
    public void canUpsert() throws Exception {

        /* when */
        WriteResult writeResult = collection.upsert("{}", "{$set:{name:'John'}}");

        /* then */
        People john = collection.findOne("{name:'John'}").as(People.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
    }

    @Test
    public void canUpsertWithWriteConcern() throws Exception {

        WriteConcern writeConcern = spy(WriteConcern.SAFE);

        /* when */
        WriteResult writeResult = collection.upsert("{}", "{$set:{name:'John'}}", writeConcern);

        /* then */
        People john = collection.findOne("{name:'John'}").as(People.class);
        assertThat(john.getName()).isEqualTo("John");
        assertThat(writeResult).isNotNull();
        verify(writeConcern).callGetLastError();
    }
}
