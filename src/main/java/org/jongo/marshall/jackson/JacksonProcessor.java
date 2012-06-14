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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper mapper;

    public JacksonProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JacksonProcessor() {
        this(createMinimalMapper());

    }

    public <T> T unmarshall(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            // TODO handle this
            throw new IllegalArgumentException("Unable to unmarshall from json: " + json + " to " + clazz, e);
        }
    }

    public <T> String marshall(T obj) {
        try {
            Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return writer.toString();
        } catch (IOException e) {
            // TODO handle this
            throw new IllegalArgumentException("Unable to marshall json from: " + obj, e);
        }
    }

    public static ObjectMapper createMinimalMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));

        SimpleModule module = new SimpleModule("jongo", new Version(1, 0, 0, null, null, null));
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        mapper.registerModule(module);

        return mapper;
    }

}
