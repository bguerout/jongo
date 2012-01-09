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
import com.mongodb.util.JSON;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_DEFAULT;

public class JsonProcessor {

    private final ObjectMapper objectMapper;

    public JsonProcessor() {
        this.objectMapper = createMapperForNonAnnotatedBean();
    }

    public <T> DBObjectMapper<T> createEntityMapper(Class<T> clazz) {
        return new DBObjectUnmarshaller(clazz, objectMapper); //TODO caching created binder should be better (a map with class as key )
    }

    public DBObject getEntityAsDBObject(Object entity) throws IOException {
        String entityAsJson = getEntityAsJson(entity);
        return ((DBObject) JSON.parse(entityAsJson));
    }

    private String getEntityAsJson(Object obj) throws IOException {
        Writer writer = new StringWriter();
        objectMapper.writeValue(writer, obj);
        return writer.toString();
    }

    private ObjectMapper createMapperForNonAnnotatedBean() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().without(FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(NON_DEFAULT));
        mapper.setVisibilityChecker(Std.defaultInstance().withFieldVisibility(ANY));
        mapper.setPropertyNamingStrategy(new MongoPropertyNamingStrategy());
        return mapper;
    }
}
