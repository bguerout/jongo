package org.jongo.spike;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.ResultMapper;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.spike.dbref.DBRefDeserializer;
import org.jongo.spike.dbref.Reference;
import org.jongo.spike.dbref.ReferenceDeserializer;
import org.jongo.util.DBObjectResultMapper;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;

public class DBRefSpikeTest extends JongoTestCase {

    private MongoCollection collection;
    private ObjectId johnId;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("buddies");
        Buddy john = new Buddy();
        john.name = "John";
        johnId = new ObjectId(collection.save(john));
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("buddies");
    }

    @Test
    public void canParseDBRef() throws Exception {
        DBRef ref = (DBRef) JSON.parse("{ '$ref' : 'users', $id : 2222 }");
        assertThat(ref.getRef()).isEqualTo("users");
    }

    @Test
    public void referenceShouldBeInserted() throws Exception {

        collection.insert("{name : 'Abby', friend: { $ref : 'buddies', $id : # }}", johnId);

        DBObject abby = collection.findOne("{name : 'Abby'}").map(new DBObjectResultMapper());

        Object ref = abby.get("friend");
        assertThat(ref).isInstanceOf(DBRef.class);
    }

    @Test
    public void referenceShouldBeFetcheableWithMapper() throws Exception {

        collection.insert("{name : 'Abby', friend: { $ref : 'buddies', $id : # }}", johnId);
        DBObject abby = collection.findOne("{name : 'Abby'}").map(new DBObjectResultMapper());

        DBRef ref = (DBRef) abby.get("friend");
        DBObject johnAsDbObject = ref.fetch();

        assertThat(johnAsDbObject.get("name")).isEqualTo("John");
    }

    @Test
    public void referenceShouldBeUnmarshalledWithJackson() throws Exception {

        MongoCollection buddies = getCollectionWithCustomMapper();
        buddies.insert("{name : 'Abby', friend: { $ref : 'buddies', $id : # }}", johnId);

        Buddy abby = buddies.findOne("{name : 'Abby'}").as(Buddy.class);

        assertThat(abby.friend).isNotNull();
        assertThat(abby.friend.name).isEqualTo("John");
    }

    @Test
    public void referenceShouldBeMarshalledWithJackson() throws Exception {

        Buddy buddy = new Buddy("Abby", new Buddy("John", null));

        collection.save(buddy);

        collection.findOne("{name : 'Abby'}").map(new ResultMapper<DBObject>() {
            public DBObject map(DBObject result) {
                assertThat(result.get("friend")).isInstanceOf(DBRef.class);
                assertThat(((DBRef) result.get("friend")).getId()).isEqualTo(johnId);
                return result;
            }
        });
    }

    private MongoCollection getCollectionWithCustomMapper() throws UnknownHostException {
        DB db = getDB();
        ObjectMapper mapper = createMapper(db);
        JacksonProcessor processor = new JacksonProcessor(mapper);
        Jongo jongo = new Jongo(db, processor, processor);
        return jongo.getCollection("buddies");
    }

    private ObjectMapper createMapper(DB database) {
        ObjectMapper mapper = JacksonProcessor.createMinimalMapper();
        SimpleModule dbRefModule = createDBRefModule(database, mapper);
        mapper.registerModule(dbRefModule);
        return mapper;
    }

    private SimpleModule createDBRefModule(DB database, ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("dbRefModule", new Version(1, 0, 0, null));
        module.addDeserializer(Reference.class, new ReferenceDeserializer(mapper, database));
        return module;
    }

    private static class Buddy {
        String name;
        @JsonDeserialize(using = DBRefDeserializer.class)
        Buddy friend;

        private Buddy(String name, Buddy friend) {
            this.friend = friend;
            this.name = name;
        }

        private Buddy() {
        }
    }
}
