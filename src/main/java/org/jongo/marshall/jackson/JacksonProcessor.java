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
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.DBObject;
import com.mongodb.LazyWriteableDBObject;
import org.bson.LazyBSONCallback;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.stream.DocumentStream;
import org.jongo.marshall.stream.DocumentStreamFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    protected static final ObjectMapperFactory OBJECT_MAPPER_FACTORY = new ObjectMapperFactory();

    private final ObjectReader reader;
    private final ObjectWriter writer;
    private final ObjectIdFieldLocator fieldLocator;

    public JacksonProcessor() {
        this(OBJECT_MAPPER_FACTORY.createBsonMapper());
    }

    public JacksonProcessor(ObjectReader reader, ObjectWriter writer) {
        this.reader = reader;
        this.writer = writer;
        this.fieldLocator = new ObjectIdFieldLocator();
    }

    public JacksonProcessor(ObjectMapper mapper) {
        this.reader = mapper.reader();
        this.writer = mapper.writer();
        this.fieldLocator = new ObjectIdFieldLocator();
    }

    public <T> T unmarshall(DBObject document, Class<T> clazz) throws MarshallingException {

        DocumentStream stream = DocumentStreamFactory.fromDBObject(document);
        try {
            return reader.withType(clazz).readValue(stream.getData(), stream.getOffset(), stream.getSize());
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, document.toString());
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshall(Object obj) throws MarshallingException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            writer.writeValue(output, obj);
        } catch (IOException e) {
            throw new MarshallingException("Unable to marshall " + obj + " into bson", e);
        }

        return new LazyWriteableDBObject(output.toByteArray(), new LazyBSONCallback());
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
