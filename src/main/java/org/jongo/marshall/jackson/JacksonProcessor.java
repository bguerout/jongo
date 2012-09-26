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
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.MappingConfig;
import org.jongo.marshall.jackson.configuration.MappingConfigBuilder;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;

import static org.jongo.marshall.jackson.configuration.MappingConfigBuilder.usingJson;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectIdFieldLocator fieldLocator;
    private final MappingConfig config;

    public JacksonProcessor() {
        this(usingJson().createConfiguration());
    }

    public JacksonProcessor(ObjectMapper bsonMapper) {
        this(new MappingConfigBuilder(bsonMapper).createConfiguration());
    }

    public JacksonProcessor(MappingConfig config) {
        this.config = config;
        this.fieldLocator = new ObjectIdFieldLocator();
    }


    public <T> T unmarshall(DBObject document, Class<T> clazz) throws MarshallingException {
        String json = document.toString();
        try {
            return config.getReader(clazz).readValue(json);
        } catch (Exception e) {
            String message = String.format("Unable to unmarshall from json: %s to %s", json, clazz);
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshall(Object obj) throws MarshallingException {
        try {
            Writer writer = new StringWriter();
            config.getWriter(obj.getClass()).writeValue(writer, obj);
            return (DBObject) JSON.parse(writer.toString());
        } catch (Exception e) {
            String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

    public void setDocumentGeneratedId(Object target, Object id) {
        Field field = fieldLocator.findFieldOrNull(target.getClass());
        if (field != null) {
            fieldLocator.updateField(target, id, field);
        }
    }
}
