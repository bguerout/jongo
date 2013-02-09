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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.jackson.id.Id;

import java.lang.reflect.Field;

public class JacksonObjectIdSelector implements ObjectIdUpdater.ObjectIdSelector {

    public boolean isAnObjectId(Field f) {
        return isId(f.getName()) || isJacksonAnnotated(f) || isIdAnnotated(f);
    }

    private boolean isJacksonAnnotated(Field f) {
        JsonProperty annotation = f.getAnnotation(JsonProperty.class);
        return annotation != null && isId(annotation.value());
    }

    private boolean isIdAnnotated(Field f) {
        Id annotation = f.getAnnotation(Id.class);
        return annotation != null;
    }

    private boolean isId(String value) {
        return "_id".equals(value);
    }
}
