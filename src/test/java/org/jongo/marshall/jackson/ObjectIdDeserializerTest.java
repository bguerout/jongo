package org.jongo.marshall.jackson;

import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ObjectIdDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ObjectIdDeserializer", new Version(1, 0, 0, null));
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        mapper.registerModule(module);
    }

    @Test
    public void shouldDeserializeId() throws Exception {

        Identifier identifier = mapper.readValue("{ \"_id\" : \"4f92d1ae44ae2dac4527d49b\"}", Identifier.class);

        assertThat(identifier._id).isEqualTo("4f92d1ae44ae2dac4527d49b");
    }

    @Test
    public void shouldDeserialize$oid() throws Exception {

        Identifier identifier = mapper.readValue("{ \"_id\" : { \"$oid\" : \"4f92d1ae44ae2dac4527d49b\"}}", Identifier.class);

        assertThat(identifier._id).isEqualTo("4f92d1ae44ae2dac4527d49b");
    }

    @Test
    public void shouldFailOnInvalidId() throws Exception {

        try {
            mapper.readValue("{ \"_id\" : { \"$invalid\" : \"wrong\"}}", Identifier.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(IllegalArgumentException.class);
        }

    }

    private static class Identifier {
        public ObjectId _id;
    }
}
