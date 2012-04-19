package org.jongo.spike.dbref;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.*;

import java.io.IOException;

public class DBRefSerializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final Class<?> rawClass;

    public DBRefSerializer() {
        this(Object.class);
    }

    private DBRefSerializer(Class<?> rawClass) {
        this.rawClass = rawClass;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Reference reference = parser.readValueAs(Reference.class);
        return reference.as(rawClass);
    }

    public JsonDeserializer createContextual(DeserializationConfig config, BeanProperty property) throws JsonMappingException {
        Class<?> propertyClass = property.getType().getRawClass();
        return new DBRefSerializer(propertyClass);
    }
}
