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
import org.jongo.marshall.jackson.JsonModule;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.jackson.bson4jackson.MongoBsonFactory;

import java.util.ArrayList;

public final class JacksonConfiguration {

    private final SimpleModule module;
    private final ArrayList<ObjectMapperBehavior> behaviors;

    public JacksonConfiguration() {
        this.module = new SimpleModule("jongo-custom-module");
        this.behaviors = new ArrayList<ObjectMapperBehavior>();
    }

    public <T> JacksonConfiguration addDeserializer(Class<T> type, JsonDeserializer<T> deserializer) {
        module.addDeserializer(type, deserializer);
        return this;
    }

    public <T> JacksonConfiguration addSerializer(Class<T> type, JsonSerializer<T> serializer) {
        module.addSerializer(type, serializer);
        return this;
    }

    public JacksonConfiguration addModule(final Module module) {
        behaviors.add(new ObjectMapperBehavior() {
            public void configure(ObjectMapper mapper) {
                mapper.registerModule(module);

            }
        });
        return this;
    }

    public JacksonConfiguration addBehaviour(ObjectMapperBehavior behavior) {
        behaviors.add(behavior);
        return this;
    }

    public ObjectMapper createJsonMapper() {
        addBehaviour(new SerializationBehavior());
        addBehaviour(new DeserializationBehavior());
        addModule(new JsonModule());
        return configureMapper(new ObjectMapper());
    }

    public ObjectMapper createBsonMapper() {
        addBehaviour(new SerializationBehavior());
        addBehaviour(new DeserializationBehavior());
        addModule(new BsonModule());
        BsonFactory bsonFactory = MongoBsonFactory.createFactory();
        return configureMapper(new ObjectMapper(bsonFactory));
    }

    public ObjectMapper configureMapper(ObjectMapper mapper) {
        addModule(module);
        for (ObjectMapperBehavior behavior : behaviors) {
            behavior.configure(mapper);
        }
        return mapper;
    }

}
