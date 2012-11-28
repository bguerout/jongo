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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonFactory;
import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.jackson.bson4jackson.MongoBsonFactory;

import java.util.ArrayList;

public class MappingConfigBuilder {
    private final SimpleModule module;
    private final ArrayList<MapperModifier> modifiers;
    private final ObjectMapper mapper;
    private ReaderCallback readerCallback;
    private WriterCallback writerCallback;

    public static MappingConfigBuilder useBson4Jackson() {
        BsonFactory bsonFactory = MongoBsonFactory.createFactory();
        return new MappingConfigBuilder(new ObjectMapper(bsonFactory))
                .addModule(new BsonModule())
                .addModifier(new SerializationModifier())
                .addModifier(new DeserializationModifier());
    }

    public MappingConfigBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
        this.module = new SimpleModule("jongo-custom-module");
        this.modifiers = new ArrayList<MapperModifier>();
        addModule(module);
    }

    public MappingConfig buildConfig() {
        for (MapperModifier modifier : modifiers) {
            modifier.modify(mapper);
        }
        setDefaultCallbacksIfNone();

        return new MappingConfig(mapper, readerCallback, writerCallback);
    }

    public Mapper buildMapper() {
        return new JacksonMapper(buildConfig());
    }

    private void setDefaultCallbacksIfNone() {
        if (readerCallback == null)
            readerCallback = new DefaultReaderCallback();
        if (writerCallback == null)
            writerCallback = new DefaultWriterCallback();
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

    public MappingConfigBuilder withView(final Class<?> viewClass) {
        setReaderCallback(new ViewReaderCallback(viewClass));
        setWriterCallback(new ViewWriterCallback(viewClass));
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

}
