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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.mongodb.DBObject;
import com.mongodb.LazyWriteableDBObject;
import org.bson.LazyBSONCallback;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;
import org.jongo.marshall.stream.DocumentStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper mapper;
    private final ObjectIdFieldLocator fieldLocator;

    public JacksonProcessor() {
        this(BsonModule.createBsonMapper());
        configureMapper(mapper);
    }

    public JacksonProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
        this.fieldLocator = new ObjectIdFieldLocator();
    }

    protected void configureMapper(ObjectMapper mapper) {
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);
        mapper.configure(AUTO_DETECT_SETTERS, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
    }

    public <T> T unmarshall(DocumentStream document, Class<T> clazz) throws MarshallingException {

        try {
            return mapper.readValue(document.getData(), document.getOffset(), document.getSize(), clazz);
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, document.toString());
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshall(Object obj) throws MarshallingException {

        ByteArrayOutputStream bsonStream = new ByteArrayOutputStream();
        try {
            mapper.writeValue(bsonStream, obj);
        } catch (IOException e) {
            throw new MarshallingException("Unable to marshall " + obj + " into bson", e);
        }

        return new LazyWriteableDBObject(bsonStream.toByteArray(), new LazyBSONCallback());
    }

    public void setDocumentGeneratedId(Object target, Object id) {
        Class<? extends Object> clazz = target.getClass();
        Field field = fieldLocator.findFieldOrNull(clazz);
        if (field != null) {
            try {

                field.setAccessible(true);
                if (field.get(target) == null) {
                    field.set(target, id);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to set objectid on class: " + clazz, e);
            }
        }
    }
}
