/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

package org.jongo.marshall.jackson;


import com.fasterxml.jackson.annotation.JsonValue;
import org.jongo.MongoCollection;
import org.jongo.util.JongoTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterBindingWithJacksonTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("marshalling");
    }

    @Test
    public void canBindEnumWithJsonValue() throws Exception {

        collection.insert("{'type':0}");

        Map result = collection.findOne("{'type':#}", Type.EMPTY).as(Map.class);

        assertThat(result).isNotNull();
    }

    @Test
    public void canBindStringWithJsonValue() throws Exception {

        collection.insert("{'prefixer':'prefix_data'}");

        Map result = collection.findOne("{'prefixer':#}", new StringWithPrefix("data")).as(Map.class);

        assertThat(result).isNotNull();
    }

    private static enum Type {
        EMPTY(0);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

    }

    private static class StringWithPrefix {

        private final String value;

        private StringWithPrefix(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return "prefix_" + value;
        }
    }
}
