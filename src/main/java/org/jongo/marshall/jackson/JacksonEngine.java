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

import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.MappingConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.jongo.marshall.jackson.configuration.MappingConfigBuilder.useBson4Jackson;


public class JacksonEngine implements Unmarshaller, Marshaller {

    private final MappingConfig config;

    public JacksonEngine() {
        this(useBson4Jackson().buildConfig());
    }

    public JacksonEngine(MappingConfig config) {
        this.config = config;
    }

    public <T> T unmarshall(BsonDocument document, Class<T> clazz) throws MarshallingException {

        try {
            return (T) config.getReader(clazz).readValue(document.toByteArray(), 0, document.getSize());
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, document.toString());
            throw new MarshallingException(message, e);
        }
    }

    public BsonDocument marshall(Object obj) throws MarshallingException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            config.getWriter(obj).writeValue(output, obj);
        } catch (IOException e) {
            throw new MarshallingException("Unable to marshall " + obj + " into bson", e);
        }
        return Bson.createDocument(output.toByteArray());
    }
}
