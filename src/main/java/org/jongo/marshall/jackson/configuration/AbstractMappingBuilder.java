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

package org.jongo.marshall.jackson.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.jackson.bson4jackson.MongoBsonFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMappingBuilder<T extends AbstractMappingBuilder<T>> {

    private final SimpleModule module = new SimpleModule("jongo-custom-module");

    private final ObjectMapper mapper;
    private final List<MapperModifier> modifiers;
    private ReaderCallback readerCallback;
    private WriterCallback writerCallback;
    private MapperModifier visibilityModifier;

    public AbstractMappingBuilder() {
        this(new ObjectMapper(MongoBsonFactory.createFactory()));
        registerModule(new BsonModule());
        addModifier(new PropertyModifier());
        addModifier(new AnnotationModifier());
    }

    public AbstractMappingBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
        this.modifiers = new ArrayList<MapperModifier>();
        registerModule(module);
    }

    protected abstract T getBuilderInstance();

    protected Mapping createMapping() {
        addModifier(visibilityModifier == null ? new VisibilityModifier() : visibilityModifier);
        for (MapperModifier modifier : modifiers) {
            modifier.modify(mapper);
        }
        setDefaultCallbacksIfNone();

        return new Mapping(mapper, readerCallback, writerCallback);
    }

    private void setDefaultCallbacksIfNone() {
        if (readerCallback == null)
            readerCallback = new DefaultReaderCallback();
        if (writerCallback == null)
            writerCallback = new DefaultWriterCallback();
    }

    public <S> T addDeserializer(Class<S> type, JsonDeserializer<S> deserializer) {
        module.addDeserializer(type, deserializer);
        return getBuilderInstance();
    }

    public <S> T addSerializer(Class<S> type, JsonSerializer<S> serializer) {
        module.addSerializer(type, serializer);
        return getBuilderInstance();
    }

    public T registerModule(final Module module) {
        modifiers.add(new MapperModifier() {
            public void modify(ObjectMapper mapper) {
                mapper.registerModule(module);
            }
        });
        return getBuilderInstance();
    }

    public T withView(Class<?> viewClass) {
        setReaderCallback(new ViewReaderCallback(viewClass));
        setWriterCallback(new ViewWriterCallback(viewClass));
        return getBuilderInstance();
    }

    public T setVisibilityChecker(final VisibilityChecker<?> visibilityChecker) {
        visibilityModifier = new MapperModifier() {
            public void modify(ObjectMapper mapper) {
                mapper.setVisibilityChecker(visibilityChecker);
            }
        };
        return getBuilderInstance();
    }

    public T enable(final DeserializationFeature feature) {
        modifiers.add(new DeserializationFeatureModifier(feature, true));
        return getBuilderInstance();
    }

    public T enable(final SerializationFeature feature) {
        modifiers.add(new SerializationFeatureModifier(feature, true));
        return getBuilderInstance();
    }

    public T enable(final MapperFeature feature) {
        modifiers.add(new MapperFeatureModifier(feature, true));
        return getBuilderInstance();
    }

    public T disable(final DeserializationFeature feature) {
        modifiers.add(new DeserializationFeatureModifier(feature, false));
        return getBuilderInstance();
    }

    public T disable(final SerializationFeature feature) {
        modifiers.add(new SerializationFeatureModifier(feature, false));
        return getBuilderInstance();
    }

    public T disable(final MapperFeature feature) {
        modifiers.add(new MapperFeatureModifier(feature, false));
        return getBuilderInstance();
    }

    public T addModifier(MapperModifier modifier) {
        modifiers.add(modifier);
        return getBuilderInstance();
    }

    public T setReaderCallback(ReaderCallback readerCallback) {
        this.readerCallback = readerCallback;
        return getBuilderInstance();
    }

    public T setWriterCallback(WriterCallback writerCallback) {
        this.writerCallback = writerCallback;
        return getBuilderInstance();
    }
}
