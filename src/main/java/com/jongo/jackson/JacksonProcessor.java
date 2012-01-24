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

import com.jongo.Marshaller;
import com.jongo.Unmarshaller;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_DEFAULT;

public class JacksonProcessor implements Unmarshaller, Marshaller {

    private final ObjectMapper mapper;

    public JacksonProcessor() {
        this.mapper = createMapperForNonAnnotatedBean();
    }

    @Override
    public <T> T unmarshall(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to unmarshall from json: " + json, e);  //TODO handle this
        }
    }

    @Override
    public <T> String marshall(T obj) {
        try {
            Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to marshall json from: " + obj, e);  //TODO handle this
        }
    }

    private ObjectMapper createMapperForNonAnnotatedBean() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().without(FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(NON_DEFAULT));
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
        mapper.setPropertyNamingStrategy(new MongoPropertyNamingStrategy());
        return mapper;
    }
}
