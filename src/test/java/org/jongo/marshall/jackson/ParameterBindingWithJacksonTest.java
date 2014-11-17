package org.jongo.marshall.jackson;


import com.fasterxml.jackson.annotation.JsonValue;
import org.jongo.MongoCollection;
import org.jongo.util.JongoTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterBindingWithJacksonTest extends JongoTestCase {

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
}
