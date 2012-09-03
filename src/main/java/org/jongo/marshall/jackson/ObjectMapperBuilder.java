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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jongo.marshall.jackson.bson4jackson.BsonMapper;

public final class ObjectMapperBuilder {

    private final SimpleModule module;
    private final ObjectMapper mapper;

    private ObjectMapperBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
        this.module = new SimpleModule("jongo-custom-simple-module");
    }

    public <T> ObjectMapperBuilder addDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
        module.addDeserializer(type, deserializer);
        return this;
    }

    public <T> ObjectMapperBuilder addSerializer(Class<T> type, JsonSerializer<T> serializer) {
        module.addSerializer(type, serializer);
        return this;
    }

    public ObjectMapperBuilder add(final Module module) {
        add(new ObjectMapperConfiguration() {
            public void configure(ObjectMapper mapper) {
                mapper.registerModule(module);

            }
        });
        return this;
    }

    public ObjectMapperBuilder add(ObjectMapperConfiguration conf) {
        conf.configure(mapper);
        return this;
    }

    public ObjectMapper getMapper() {
        add(module);
        return mapper;
    }

    public JacksonProcessor createProcessor() {
        return new JacksonProcessor(getMapper());
    }

    public static ObjectMapperBuilder useJongoMapper() {
        ObjectMapper bsonMapper = BsonMapper.createMapper();
        return new ObjectMapperBuilder(bsonMapper)
                .add(new SerializationConfiguration())
                .add(new DeserializationConfiguration());
    }

    public static ObjectMapperBuilder useNewMapper() {
        return new ObjectMapperBuilder(new ObjectMapper());
    }
}
