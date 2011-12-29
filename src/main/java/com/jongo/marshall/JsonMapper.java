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

package com.jongo.marshall;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_DEFAULT;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class JsonMapper {

    private final ObjectMapper mapper;

    public JsonMapper() {
	this.mapper = createConfLessMapper();
    }

    public <T> T getEntity(String json, Class<T> clazz) throws IOException {
	return mapper.readValue(json, clazz);
    }

    public DBObject convert(String jsonQuery) {
	return ((DBObject) JSON.parse(jsonQuery));
    }

    public DBObject convert(Object obj) throws IOException {
	Writer writer = new StringWriter();
	mapper.writeValue(writer, obj);
	return convert(writer.toString());
    }

    private ObjectMapper createConfLessMapper() {
	ObjectMapper mapper = new ObjectMapper();
	mapper.setDeserializationConfig(mapper.getDeserializationConfig().without(FAIL_ON_UNKNOWN_PROPERTIES));
	mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(NON_DEFAULT));
	mapper.setVisibilityChecker(Std.defaultInstance().withFieldVisibility(ANY));

	mapper.setPropertyNamingStrategy(new MongoPropertyNamingStrategy());
	return mapper;
    }
}
