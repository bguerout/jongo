package org.jongo.spike.dbref;

import com.mongodb.DB;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.StdDeserializationContext;
import org.codehaus.jackson.map.module.SimpleModule;
import org.fest.assertions.Assertions;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ReferenceDeserializerTest {

    @Test
    public void shouldDeserializeBSONDBref() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        DB db = mock(DB.class);
        SimpleModule module = new SimpleModule("dbRefModule", new Version(1, 0, 0, null));
        module.addDeserializer(Reference.class, new ReferenceDeserializer(mapper, db));
        mapper.registerModule(module);

        Reference reference = mapper.readValue("{\"$ref\":\"aCollection\",\"$id\":{\"$oid\":\"4f916b11e4b03bf323284f86\"}}", Reference.class);

        DBRef dbRef = reference.getDbRef();
        assertThat(dbRef.getDB()).isEqualTo(db);
        assertThat(dbRef.getRef()).isEqualTo("aCollection");
        assertThat(dbRef.getId()).isEqualTo(new ObjectId("4f916b11e4b03bf323284f86"));
    }


}
