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

import com.mongodb.DBObject;
import com.mongodb.LazyWriteableDBObject;
import org.bson.LazyBSONCallback;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.JacksonConfig;
import org.jongo.marshall.stream.BsonStream;
import org.jongo.marshall.stream.BsonStreamFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.jongo.marshall.jackson.configuration.JacksonConfigBuilder.usingStream;

public class StreamProcessor implements Unmarshaller, Marshaller {

    private final ObjectIdFieldLocator fieldLocator;
    private final JacksonConfig config;

    public StreamProcessor() {
        this(usingStream().createConfiguration());
    }

    public StreamProcessor(JacksonConfig config) {
        this.config = config;
        this.fieldLocator = new ObjectIdFieldLocator();
    }

    public <T> T unmarshall(DBObject document, Class<T> clazz) throws MarshallingException {

        BsonStream stream = BsonStreamFactory.fromDBObject(document);
        try {
            return config.getReader(clazz).readValue(stream.getData(), stream.getOffset(), stream.getSize());
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, document.toString());
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshall(Object obj) throws MarshallingException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            config.getWriter(obj).writeValue(output, obj);
        } catch (IOException e) {
            throw new MarshallingException("Unable to marshall " + obj + " into bson", e);
        }

        return new LazyWriteableDBObject(output.toByteArray(), new LazyBSONCallback());
    }

    public void setDocumentGeneratedId(Object target, Object id) {
        Field field = fieldLocator.findFieldOrNull(target.getClass());
        if (field != null) {
            fieldLocator.updateField(target, id, field);
        }
    }

}
