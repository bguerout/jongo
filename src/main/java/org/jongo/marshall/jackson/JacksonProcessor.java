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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jongo.marshall.BSONPrimitives;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper mapper;

    public JacksonProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JacksonProcessor() {
        this(createPreConfiguredMapper());

    }

    public <T> T unmarshall(String json, Class<T> clazz) throws MarshallingException {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            String message = String.format("Unable to unmarshall from json: %s to %s", json, clazz);
            throw new MarshallingException(message, e);
        }
    }

    public <T> String marshall(T obj) throws MarshallingException {
        try {
            Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return writer.toString();
        } catch (Exception e) {
            String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

    public static ObjectMapper createPreConfiguredMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);
        mapper.configure(AUTO_DETECT_SETTERS, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));

        SimpleModule module = new SimpleModule("jongo", new Version(1, 0, 0, null, null, null));
        addBSONTypeSerializers(module);
        mapper.registerModule(module);
        return mapper;
    }

    private static void addBSONTypeSerializers(SimpleModule module) {
        NativeSerializer serializer = new NativeSerializer();
        NativeDeserializer deserializer = new NativeDeserializer();
        for (Class primitive : BSONPrimitives.getPrimitives()) {
            module.addSerializer(primitive, serializer);
            module.addDeserializer(primitive, deserializer);
        }
        module.addDeserializer(Date.class, new BackwardDateDeserializer(deserializer));
    }

}
