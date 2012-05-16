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

package org.jongo.spike.dbref.jackson;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.*;

import java.io.IOException;

public class DBRefDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final Class<?> rawClass;

    //Jackson's constructor
    public DBRefDeserializer() {
        this(Object.class);
    }

    private DBRefDeserializer(Class<?> rawClass) {
        this.rawClass = rawClass;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Reference reference = parser.readValueAs(Reference.class);
        return reference.as(rawClass);
    }

    public JsonDeserializer createContextual(DeserializationConfig config, BeanProperty property) throws JsonMappingException {
        Class<?> propertyClass = property.getType().getRawClass();
        return new DBRefDeserializer(propertyClass);
    }
}
