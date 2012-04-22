package org.jongo.spike.dbref;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.jongo.spike.dbref.jackson.Reference;
import org.jongo.spike.dbref.jackson.ReferenceLink;
import org.jongo.spike.dbref.jackson.ReferenceSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

public class ReferenceSerializerTest {

    private ObjectMapper mapper;
    private ReferenceSerializer serializer;
    private Buddy buddyWithAFriend;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        serializer = new ReferenceSerializer();
        buddyWithAFriend = new Buddy("john", new Buddy("pal", null));

        SimpleModule module = new SimpleModule("module", new Version(1, 0, 0, null));
        module.addSerializer(Reference.class, serializer);
        mapper.registerModule(module);
    }

    @Test
    public void shouldFailWhenNoLinksRegistered() throws Exception {
        try {
            mapper.writeValueAsString(buddyWithAFriend);
            fail("Should have thrown an exception");
        } catch (IOException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void shouldFailWhenObjectHasANullId() throws Exception {
        serializer.registerReferenceLink(Buddy.class, new FakeReferenceLink(withNullId()));

        try {
            mapper.writeValueAsString(buddyWithAFriend);
            fail("Should have thrown an exception");
        } catch (IOException e) {
            assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
        }

    }

    @Test
    public void shouldSerializeDBRef() throws Exception {

        serializer.registerReferenceLink(Buddy.class, new FakeReferenceLink("idOfAFriend"));

        String asString = mapper.writeValueAsString(buddyWithAFriend);

        assertThat(asString).contains("{\"friend\":{ \"$ref\" : \"buddies\", \"$id\" : \"idOfAFriend\" }");


    }

    private String withNullId() {
        return null;
    }


    private static class FakeReferenceLink implements ReferenceLink<Buddy> {

        private String id;

        private FakeReferenceLink(String id) {
            this.id = id;
        }

        public String getReferenceCollectionName(Buddy buddy) {
            return "buddies";
        }

        public String getId(Buddy buddy) {
            return id;
        }
    }
}
