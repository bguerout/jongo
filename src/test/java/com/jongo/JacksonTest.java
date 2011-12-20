package com.jongo;

import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

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
    public void shouldUnMarshall() throws IOException
    {
        Poi poi = Jongo.unmarshallString("{\"address\":\"22 rue des murlins\"}", Poi.class);

        assertThat(poi.address).isEqualTo("22 rue des murlins");

    }

}
