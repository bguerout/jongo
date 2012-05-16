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

import com.mongodb.DBRef;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReferenceSerializer extends JsonSerializer<Object> {

    private Map<Class<?>, ReferenceLink<?>> referenceLinks = new HashMap<Class<?>, ReferenceLink<?>>();

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

        if (!referenceLinks.containsKey(value.getClass())) {
            throw new IllegalArgumentException("Unable to serialize " + value.getClass() + ", no translators has been defined.");
        }

        ReferenceLink link = referenceLinks.get(value.getClass());
        String json = new DBRef(null, link.getReferenceCollectionName(value), getId(value, link)).toString();
        jgen.writeRawValue(json);
    }

    private <T> String getId(T value, ReferenceLink<T> link) {

        String id = link.getId(value);
        if (id == null) {
            throw new NullPointerException("Cannot create DRRef because its id is null into " + value);
        }
        return id;
    }

    public <T> void registerReferenceLink(Class<T> typeClass, ReferenceLink<T> link) {
        referenceLinks.put(typeClass, link);
    }

}
