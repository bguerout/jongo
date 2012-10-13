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
import org.jongo.bson.BsonByte;
import org.jongo.bson.BsonByteFactory;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.MappingConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.jongo.marshall.jackson.configuration.MappingConfigBuilder.usingBson;

public class BsonEngine implements Unmarshaller, Marshaller {

    private final MappingConfig config;

    public BsonEngine() {
        this(usingBson().build());
    }

    public BsonEngine(MappingConfig config) {
        this.config = config;
    }

    public <T> T unmarshall(DBObject document, Class<T> clazz) throws MarshallingException {

        BsonByte stream = BsonByteFactory.fromDBObject(document);
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

        return convertToDBObject(output.toByteArray());
    }

    protected DBObject convertToDBObject(byte[] bytes) {
        return new LazyWriteableDBObject(bytes, new LazyBSONCallback());
    }

}
