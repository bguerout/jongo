package org.jongo.marshall.jackson.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static org.fest.assertions.Assertions.assertThat;

public class MappingTest {

    @Test
    public void shouldNotAddBsonConfWithCustomMapper() throws Exception {
        Mapping.Builder builder = new Mapping.Builder(new ObjectMapper());
        Mapping mapping = builder.build();
        ObjectId id = ObjectId.get();//serialized using bson serializer
        Friend friend = new Friend(id, "John");

        Writer writer = new StringWriter();
        mapping.getWriter(friend).writeValue(writer, friend);

        assertThat(writer.toString()).contains("John");
    }
}
