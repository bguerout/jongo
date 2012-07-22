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
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.jongo.marshall.jackson.bson4jackson.BsonModule;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_GETTERS;
import static com.fasterxml.jackson.databind.MapperFeature.AUTO_DETECT_SETTERS;

public class ObjectMapperFactory {

    public ObjectMapper createBsonMapper() {

        BsonModule module = new BsonModule();
        ObjectMapper mapper = new ObjectMapper(module.createJsonFactory());
        mapper.registerModule(module);
        configureMapper(mapper);
        return mapper;
    }

    public void configureMapper(ObjectMapper mapper) {
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(AUTO_DETECT_GETTERS, false);
        mapper.configure(AUTO_DETECT_SETTERS, false);
        mapper.setSerializationInclusion(NON_NULL);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
    }
}
