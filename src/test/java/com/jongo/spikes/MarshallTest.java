/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
