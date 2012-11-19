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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonFactory;
import org.jongo.Provider;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.jackson.bson4jackson.MongoBsonFactory;
import org.jongo.marshall.jackson.configuration.JsonModule;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import org.jongo.marshall.jackson.configuration.ReaderCallback;
import org.jongo.marshall.jackson.configuration.WriterCallback;

import java.util.ArrayList;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;
import static org.jongo.marshall.jackson.JacksonProviders.Type.BSON;
import static org.jongo.marshall.jackson.JacksonProviders.Type.JSON;

public class JacksonProviders {

    public static Builder usingBson() {
        return Builder.usingBson();
    }

    public static Builder usingJson() {
        return Builder.usingJson();
    }

    public enum Type {
        BSON, JSON;
    }

    public static class Builder {
        private final Type type;
        private final SimpleModule module;
        private final ArrayList<MapperModifier> modifiers;
        private final ObjectMapper mapper;
        private ReaderCallback readerCallback;
        private WriterCallback writerCallback;

        public Builder(ObjectMapper mapper, Type type) {
            this.type = type;
            this.mapper = mapper;
            this.module = new SimpleModule("jongo-custom-module");
            this.modifiers = new ArrayList<MapperModifier>();
            addModule(module);
        }

        public static Builder usingJson() {
            return new Builder(new ObjectMapper(), JSON)
                    .addModule(new JsonModule())
                    .addModifier(new SerializationModifier())
                    .addModifier(new DeserializationModifier());
        }

        public static Builder usingBson() {
            BsonFactory bsonFactory = MongoBsonFactory.createFactory();
            return new Builder(new ObjectMapper(bsonFactory), BSON)
                    .addModule(new BsonModule())
                    .addModifier(new SerializationModifier())
                    .addModifier(new DeserializationModifier());
        }

        protected MappingConfig innerConfig() {
            for (MapperModifier modifier : modifiers) {
                modifier.modify(mapper);
            }
            setDefaultCallbacksIfNone();

            return new MappingConfig(mapper, readerCallback, writerCallback);
        }

        private void setDefaultCallbacksIfNone() {
            if (readerCallback == null)
                readerCallback = new DefaultReaderCallback();
            if (writerCallback == null)
                writerCallback = new DefaultWriterCallback();
        }

        public Provider build() {
            MappingConfig config = innerConfig();
            if (type == BSON)
                return new BsonProvider(config);
            else
                return new JsonProvider(config);
        }

        public <T> Builder addDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
            module.addDeserializer(type, deserializer);
            return this;
        }

        public <T> Builder addSerializer(Class<T> type, JsonSerializer<T> serializer) {
            module.addSerializer(type, serializer);
            return this;
        }

        public Builder addModule(final Module module) {
            modifiers.add(new MapperModifier() {
                public void modify(ObjectMapper mapper) {
                    mapper.registerModule(module);
                }
            });
            return this;
        }

        public Builder withView(final Class<?> viewClass) {
            setReaderCallback(new ViewReaderCallback(viewClass));
            setWriterCallback(new ViewWriterCallback(viewClass));
            return this;
        }

        public Builder addModifier(MapperModifier modifier) {
            modifiers.add(modifier);
            return this;
        }

        public Builder setReaderCallback(ReaderCallback readerCallback) {
            this.readerCallback = readerCallback;
            return this;
        }

        public Builder setWriterCallback(WriterCallback writerCallback) {
            this.writerCallback = writerCallback;
            return this;
        }
    }

    public static final class SerializationModifier implements MapperModifier {

        public void modify(ObjectMapper mapper) {
            mapper.disable(AUTO_DETECT_GETTERS);
            mapper.setSerializationInclusion(NON_NULL);
            VisibilityChecker<?> checker = mapper.getSerializationConfig().getDefaultVisibilityChecker();
            mapper.setVisibilityChecker(checker.withFieldVisibility(ANY));
        }
    }

    public static final class DeserializationModifier implements MapperModifier {

        public void modify(ObjectMapper mapper) {
            mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.disable(AUTO_DETECT_SETTERS);
        }
    }

    private static class ViewWriterCallback implements WriterCallback {
        private final Class<?> viewClass;

        public ViewWriterCallback(Class<?> viewClass) {
            this.viewClass = viewClass;
        }

        public ObjectWriter getWriter(ObjectMapper mapper, Object pojo) {
            return mapper.writerWithView(viewClass);
        }
    }

    private static class ViewReaderCallback implements ReaderCallback {
        private final Class<?> viewClass;

        public ViewReaderCallback(Class<?> viewClass) {
            this.viewClass = viewClass;
        }

        public ObjectReader getReader(ObjectMapper mapper, Class<?> clazz) {
            return mapper.reader(clazz).withView(viewClass);
        }
    }

    private static class DefaultWriterCallback implements WriterCallback {
        public ObjectWriter getWriter(ObjectMapper mapper, Object pojo) {
            return mapper.writer();
        }
    }

    private static class DefaultReaderCallback implements ReaderCallback {
        public ObjectReader getReader(ObjectMapper mapper, Class<?> clazz) {
            return mapper.reader(clazz);
        }
    }
}
