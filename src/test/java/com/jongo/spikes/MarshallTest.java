package com.jongo.spikes;

import com.jongo.Coordinate;
import com.jongo.ObjectMapperFactory;
import com.jongo.Poi;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.fest.assertions.Assertions.assertThat;

public class MarshallTest {


    private String marshallQuery(Object obj) throws IOException {
        Writer writer = new StringWriter();
        ObjectMapperFactory.createConfLessMapper().writeValue(writer, obj);
        return writer.toString();
    }

    @Test
    public void shouldMarshall() throws IOException {
        Poi poi = new Poi("22 rue des murlins");

        String json = marshallQuery(poi);

        assertThat(json).isEqualTo("{\"address\":\"22 rue des murlins\"}");
    }

    @Test
    public void shouldMarshallComplexType() throws IOException {

        Poi poi = new Poi("22 rue des murlins");
        poi.setCoordinate(new Coordinate(48, 2));

        String json = marshallQuery(poi);

        assertThat(json).isEqualTo("{\"address\":\"22 rue des murlins\",\"coordinate\":{\"lat\":48,\"lng\":2}}");
    }
}
