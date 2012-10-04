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

package org.jongo.marshall.jackson.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonFactory;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.jackson.bson4jackson.MongoBsonFactory;

import java.util.ArrayList;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;

public final class MappingConfigBuilder {

    private final SimpleModule module;
    private final ArrayList<MapperModifier> modifiers;
    private final ObjectMapper mapper;
    private ReaderCallback readerCallback;
    private WriterCallback writerCallback;

    public MappingConfigBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
        this.module = new SimpleModule("jongo-custom-module");
        this.modifiers = new ArrayList<MapperModifier>();
        this.readerCallback = new DefaultReaderCallback();
        this.writerCallback = new DefaultWriterCallback();
        addModule(module);
    }

    public <T> MappingConfigBuilder addDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
        module.addDeserializer(type, deserializer);
        return this;
    }

    public <T> MappingConfigBuilder addSerializer(Class<T> type, JsonSerializer<T> serializer) {
        module.addSerializer(type, serializer);
        return this;
    }

    public MappingConfigBuilder addModule(final Module module) {
        modifiers.add(new MapperModifier() {
            public void modify(ObjectMapper mapper) {
                mapper.registerModule(module);
            }
        });
        return this;
    }

    public MappingConfigBuilder addModifier(MapperModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    public MappingConfigBuilder setReaderCallback(ReaderCallback readerCallback) {
        this.readerCallback = readerCallback;
        return this;
    }

    public MappingConfigBuilder setWriterCallback(WriterCallback writerCallback) {
        this.writerCallback = writerCallback;
        return this;
    }

    public MappingConfig build() {
        for (MapperModifier modifier : modifiers) {
            modifier.modify(mapper);
        }
        return new MappingConfig(mapper, readerCallback, writerCallback);
    }

    public static MappingConfigBuilder usingJson() {
        return new MappingConfigBuilder(new ObjectMapper())
                .addModule(new JsonModule())
                .addModifier(new SerializationModifier())
                .addModifier(new DeserializationModifier());
    }

    public static MappingConfigBuilder usingStream() {
        BsonFactory bsonFactory = MongoBsonFactory.createFactory();
        return new MappingConfigBuilder(new ObjectMapper(bsonFactory))
                .addModule(new BsonModule())
                .addModifier(new SerializationModifier())
                .addModifier(new DeserializationModifier());
    }


    private static class DefaultReaderCallback implements ReaderCallback {
        public ObjectReader getReader(ObjectMapper mapper, Class<?> clazz) {
            return mapper.reader(clazz);
        }
    }

    private static class DefaultWriterCallback implements WriterCallback {
        public ObjectWriter getWriter(ObjectMapper mapper, Object pojo) {
            return mapper.writer();
        }
    }

    public static final class DeserializationModifier implements MapperModifier {

        public void modify(ObjectMapper mapper) {
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(AUTO_DETECT_SETTERS, false);
        }
    }

    public static final class SerializationModifier implements MapperModifier {

        public void modify(ObjectMapper mapper) {

            mapper.configure(AUTO_DETECT_GETTERS, false);
            mapper.setSerializationInclusion(NON_NULL);
            VisibilityChecker<?> checker = mapper.getSerializationConfig().getDefaultVisibilityChecker();
            mapper.setVisibilityChecker(checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        }
    }
}
