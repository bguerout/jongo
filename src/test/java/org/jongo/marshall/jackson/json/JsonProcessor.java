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

package org.jongo.marshall.jackson.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.marshall.stream.DocumentStream;

import java.io.StringWriter;
import java.io.Writer;

public class JsonProcessor extends JacksonProcessor {

    private final ObjectMapper mapper;

    public JsonProcessor() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JsonModule());
        OBJECT_MAPPER_FACTORY.configureMapper(this.mapper);
    }

    @Override
    public <T> T unmarshall(DocumentStream document, Class<T> clazz) throws MarshallingException {
        String json = document.asBSONObject().toString();
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            String message = String.format("Unable to unmarshall from json: %s to %s", json, clazz);
            throw new MarshallingException(message, e);
        }
    }

    @Override
    public DBObject marshall(Object obj) throws MarshallingException {
        try {
            Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return (DBObject) JSON.parse(writer.toString());
        } catch (Exception e) {
            String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

}
