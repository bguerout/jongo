package org.jongo.marshall;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class NativeMarshallerTest {

    private NativeMarshaller marshaller;

    @Before
    public void setUp() throws Exception {
        marshaller = new NativeMarshaller();
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailWithCharParameter() throws Exception {
        char c = '1';

        marshaller.marshall(c);
    }

    @Test
    public void canMapParameter() throws Exception {

        String query = marshaller.marshall("123");

        assertThat(query).isEqualTo("\"123\"");
    }


    @Test
    public void canMapDate() throws Exception {

        Date epoch = new Date(0);

        String query = marshaller.marshall(epoch);

        assertThat(query).isEqualTo("{ \"$date\" : \"1970-01-01T00:00:00.000Z\"}");
    }

    @Test
    public void canMapList() throws Exception {

        List<String> elements = new ArrayList<String>();
        elements.add("1");
        elements.add("2");

        String query = marshaller.marshall(elements);

        assertThat(query).isEqualTo("[ \"1\" , \"2\"]");
    }

    @Test
    public void canHandleBoolean() throws Exception {

        String query = marshaller.marshall(true);

        assertThat(query).isEqualTo("true");
    }

    @Test
    public void shouldEscapeJsonAsString() throws Exception {

        String query = marshaller.marshall("{injection:true}");

        assertThat(query).isEqualTo("\"{injection:true}\"");
    }


    @Test
    public void canHandleObjectId() throws Exception {

        String query = marshaller.marshall(new ObjectId("47cc67093475061e3d95369d"));

        assertThat(query).isEqualTo("{ \"$oid\" : \"47cc67093475061e3d95369d\"}");
    }


}
