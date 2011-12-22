package com.jongo;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class JsonMapperTest {
    private JsonMapper jsonMapper;

    @Before
    public void setUp() throws Exception {
        jsonMapper = new JsonMapper();
    }


    @Test
    public void canConvertQueryResultToObjects() throws IOException {
        Poi poi = jsonMapper.getEntity("{\"address\":\"22 rue des murlins\",\"coordinate\":{\"lat\":48,\"lng\":2}}", Poi.class);

        assertThat(poi.coordinate.lat).isEqualTo(48);
        assertThat(poi.coordinate.lng).isEqualTo(2);
    }

    @Test
    public void canConvertQueryResultToObject() throws IOException {

        Poi poi = jsonMapper.getEntity("{\"address\":\"22 rue des murlins\"}", Poi.class);

        assertThat(poi.address).isEqualTo("22 rue des murlins");
    }

}
