package org.jongo.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.bson.types.ObjectId;

import java.io.IOException;

public class QueryModule extends Module {

    @Override
    public String getModuleName() {
        return "QueryModule";
    }

    @Override
    public Version version() {
        return new Version(2, 0, 0, "", "org.jongo", "querymodule");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new QuerySerializers());
        context.addDeserializers(new QueryDeserializers());
    }


    private static class QueryDeserializers extends SimpleDeserializers {
        public QueryDeserializers() {
            addDeserializer(_id, _idDeserializer());
        }
    }

    private static class QuerySerializers extends SimpleSerializers {
        public QuerySerializers() {
            addSerializer(_id, _idSerializer());
        }
    }

    private static Class<ObjectId> _id = ObjectId.class;

    private static JsonDeserializer<ObjectId> _idDeserializer() {
        return new JsonDeserializer<ObjectId>() {
            public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                return new ObjectId(jp.readValueAs(String.class));
            }
        };
    }

    private static JsonSerializer<ObjectId> _idSerializer() {
        return new JsonSerializer<ObjectId>() {
            public void serialize(ObjectId obj, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
                jsonGenerator.writeObject(obj == null ? null : new ObjectId(obj.toString()));
            }
        };
    }
}
