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

import java.util.ArrayList;

import org.jongo.Mapper;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.jackson.bson4jackson.MongoBsonFactory;
import org.jongo.marshall.jackson.configuration.DefaultReaderCallback;
import org.jongo.marshall.jackson.configuration.DefaultWriterCallback;
import org.jongo.marshall.jackson.configuration.DeserializationModifier;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.marshall.jackson.configuration.ReaderCallback;
import org.jongo.marshall.jackson.configuration.SerializationModifier;
import org.jongo.marshall.jackson.configuration.ViewReaderCallback;
import org.jongo.marshall.jackson.configuration.ViewWriterCallback;
import org.jongo.marshall.jackson.configuration.WriterCallback;
import org.jongo.query.JsonQueryFactory;
import org.jongo.query.QueryFactory;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacksonMapper implements Mapper {

    private final JacksonEngine engine;
    private final JacksonObjectIdUpdater objectIdUpdater;
    private final JsonQueryFactory queryFactory;

    private JacksonMapper(Mapping mapping) {
        this.engine = new JacksonEngine(mapping);
        this.queryFactory = new JsonQueryFactory(engine);
        this.objectIdUpdater = new JacksonObjectIdUpdater();
    }
    
    public Marshaller getMarshaller() {
        return engine;
    }

    public Unmarshaller getUnmarshaller() {
        return engine;
    }

    public ObjectIdUpdater getObjectIdUpdater() {
        return objectIdUpdater;
    }

    public QueryFactory getQueryFactory() {
        return queryFactory;
    }
    
    public static class Builder {
        private final SimpleModule module;
        private final ArrayList<MapperModifier> modifiers;
        private final ObjectMapper mapper;
        private ReaderCallback readerCallback;
        private WriterCallback writerCallback;

        public Builder() {
            this(new ObjectMapper(MongoBsonFactory.createFactory()));
            addModule(new BsonModule());
            addModifier(new SerializationModifier());
            addModifier(new DeserializationModifier());
        }

        public Builder(ObjectMapper mapper) {
            this.mapper = mapper;
            this.module = new SimpleModule("jongo-custom-module");
            this.modifiers = new ArrayList<MapperModifier>();
            addModule(module);
        }

        public Mapper build() {
            return new JacksonMapper(innerConfig());
        }
        
        public Mapping innerConfig() {
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
}
