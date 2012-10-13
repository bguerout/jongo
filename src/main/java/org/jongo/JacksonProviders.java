package org.jongo;

import static org.jongo.JacksonProviders.Type.JSON;
import static org.jongo.JacksonProviders.Type.BSON;

import org.jongo.marshall.jackson.BsonProvider;
import org.jongo.marshall.jackson.JsonProvider;
import org.jongo.marshall.jackson.configuration.MappingConfigBuilder;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;

public class JacksonProviders {

    public static Builder usingBson() {
        return new Builder(BSON);
    }

    public static Builder usingJson() {
        return new Builder(JSON);
    }

    public enum Type {
        BSON, JSON;
    }

    public static class Builder {
        private MappingConfigBuilder builder;
        private Type type;

        public Builder(Type type) {
            this.type = type;
            switch (type) {
                case BSON:
                    builder = MappingConfigBuilder.usingBson();
                    break;
                case JSON:
                    builder = MappingConfigBuilder.usingJson();
            }
        }

        public Builder addModule(Module module) {
            builder.addModule(module);
            return this;
        }

        public <T> Builder addDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
            builder.addDeserializer(type, deserializer);
            return this;
        }

        public <T> Builder addSerializer(Class<T> type, JsonSerializer<T> serializer) {
            builder.addSerializer(type, serializer);
            return this;
        }

        public Provider build() {
            if (type == BSON)
                return new BsonProvider(builder.build());
            else
                return new JsonProvider(builder.build());
        }
    }
}