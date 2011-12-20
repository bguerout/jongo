package com.jongo;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

public class JacksonTest
{
    @Test
    public void shouldMarshall() throws IOException
    {
        Poi poi = new Poi("22 rue des murlins");

        String json = Jongo.marshallQuery(poi);

        assertThat(json).isEqualTo("{\"address\":\"22 rue des murlins\"}");
    }

    @Test
    public void shouldUnmarshall() throws IOException
    {
        Poi poi = Jongo.unmarshallString("{\"address\":\"22 rue des murlins\"}", Poi.class);

        assertThat(poi.address).isEqualTo("22 rue des murlins");
    }

    @Test
    public void shouldMarshallComplex() throws IOException
    {
        Poi poi = new Poi("22 rue des murlins");
        poi.coordinate = new Coordinate(48, 2);

        String json = Jongo.marshallQuery(poi);

        assertThat(json).isEqualTo("{\"address\":\"22 rue des murlins\",\"coordinate\":{\"lat\":48,\"lng\":2}}");
    }

    @Test
    public void shouldUnmarshallComplex() throws IOException
    {
        Poi poi = Jongo.unmarshallString("{\"address\":\"22 rue des murlins\",\"coordinate\":{\"lat\":48,\"lng\":2}}", Poi.class);

        assertThat(poi.coordinate.lat).isEqualTo(48);
        assertThat(poi.coordinate.lng).isEqualTo(2);
    }
}
