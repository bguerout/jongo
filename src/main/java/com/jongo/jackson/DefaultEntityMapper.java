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

package com.jongo.jackson;

import com.jongo.DBObjectMapper;
import com.mongodb.DBObject;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import static com.jongo.MongoCollection.MONGO_ID;

public class DefaultEntityMapper<T> implements DBObjectMapper<T> {

    private final Class<T> clazz;
    private final ObjectMapper mapper;

    public DefaultEntityMapper(Class<T> clazz, ObjectMapper mapper) {
        this.clazz = clazz;
        this.mapper = mapper;
    }

    @Override
    public T map(DBObject dbObject) {
        setIdProperly(dbObject);
        String json = dbObject.toString();
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create an entity from json: " + json, e);  //TODO handle this
        }
    }

    private void setIdProperly(DBObject dbObject) {
        Object id = dbObject.get(MONGO_ID);
        if (id != null)
            dbObject.put(MONGO_ID, id.toString());
    }
}
