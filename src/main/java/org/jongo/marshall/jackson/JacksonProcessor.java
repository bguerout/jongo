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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.LazyWriteableDBObject;
import org.bson.LazyBSONCallback;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper bsonMapper;
    private ObjectMapper jsonMapper;

    public JacksonProcessor() {
        this(ObjectMapperFactory.createBsonMapper(), ObjectMapperFactory.createJsonMapper());
    }

    public JacksonProcessor(ObjectMapper bsonMapper, ObjectMapper jsonMapper) {
        this.bsonMapper = bsonMapper;
        this.jsonMapper = jsonMapper;
    }

    public <T> T unmarshall(byte[] data, int offset, Class<T> clazz) throws MarshallingException {

        try {
            return bsonMapper.readValue(data, offset, data.length - offset, clazz);
        } catch (IOException e) {
            throw new MarshallingException("Unable to unmarshall result into " + clazz, e);
        }
    }

    public String marshallAsJson(Object obj) throws MarshallingException {
        try {
            Writer writer = new StringWriter();
            jsonMapper.writeValue(writer, obj);
            return writer.toString();
        } catch (Exception e) {
            String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshallAsBson(Object obj) throws MarshallingException {

        ByteArrayOutputStream bsonStream = new ByteArrayOutputStream();
        try {
            bsonMapper.writeValue(bsonStream, obj);
        } catch (IOException e) {
            throw new MarshallingException("Unable to marshall " + obj + " into bson", e);
        }

        return new LazyWriteableDBObject(bsonStream.toByteArray(), new LazyBSONCallback());
    }

    public void setDocumentGeneratedId(Object document, Object id) {
        Field field = findIdFieldForClass(document.getClass());
        try {
            if (field != null) {
                field.setAccessible(true);
                if (field.get(document) == null) {
                    field.set(document, id);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to set objectid on class: " + document.getClass(), e);
        }
    }

    private Field findIdFieldForClass(Class<?> clazz) {

        do {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType().equals(ObjectId.class)) {
                    JsonProperty annotation = f.getAnnotation(JsonProperty.class);
                    if (isId(f.getName()) || annotation != null && isId(annotation.value())) {
                        return f;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));

        return null;
    }

    private boolean isId(String value) {
        return MongoCollection.MONGO_DOCUMENT_ID_NAME.equals(value);
    }
}
