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
import org.jongo.marshall.stream.DocumentStream;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper documentMapper;

    public JacksonProcessor() {
        this(ObjectMapperFactory.createBsonMapper());
    }

    public JacksonProcessor(ObjectMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    public <T> T unmarshall(DocumentStream document, Class<T> clazz) throws MarshallingException {

        try {
            return documentMapper.readValue(document.getData(), document.getOffset(), document.getSize(), clazz);
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, document.toString());
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshall(Object obj) throws MarshallingException {

        ByteArrayOutputStream bsonStream = new ByteArrayOutputStream();
        try {
            documentMapper.writeValue(bsonStream, obj);
        } catch (IOException e) {
            throw new MarshallingException("Unable to marshall " + obj + " into bson", e);
        }

        return new LazyWriteableDBObject(bsonStream.toByteArray(), new LazyBSONCallback());
    }

    public void setDocumentGeneratedId(Object target, Object id) {
        Field field = findIdFieldForClass(target.getClass());
        try {
            if (field != null) {
                field.setAccessible(true);
                if (field.get(target) == null) {
                    field.set(target, id);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to set objectid on class: " + target.getClass(), e);
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
