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
import org.bson.types.ObjectId;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.jackson.id.Id;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JacksonObjectIdUpdater implements ObjectIdUpdater {

    private final Map<Class<?>, Field> idFields = new HashMap<Class<?>, Field>();
    private final ObjectIdSelector fieldSelector;

    public JacksonObjectIdUpdater() {
        fieldSelector = new ObjectIdSelector();
    }

    public boolean canSetObjectId(Object target) {
        Field field = findFieldOrNull(target.getClass());
        return field != null && getTargetValue(target, field) == null;
    }

    public void setDocumentGeneratedId(Object target, ObjectId id) {
        Field field = findFieldOrNull(target.getClass());
        if (field == null) {
            throw new IllegalArgumentException("Unable to set objectid on class: " + target.getClass());
        }
        updateField(target, id, field);
    }

    protected Field findFieldOrNull(Class<?> clazz) {
        if (idFields.containsKey(clazz)) {
            return idFields.get(clazz);
        }

        while (!Object.class.equals(clazz)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            if (declaredFields == null) {
                return null;
            }
            for (Field f : declaredFields) {
                if (fieldSelector.canBeConsideredAsObjectId(f)) {
                    idFields.put(clazz, f);
                    return f;
                }
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

    protected void updateField(Object target, ObjectId id, Field field) {
        Object value = getTargetValue(target, field);
        if (value == null) {
            try {
                if (field.getType().equals(ObjectId.class)) {
                    field.set(target, id);
                } else if (field.getType().equals(String.class)) {
                    field.set(target, id.toString());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to set objectid on class: " + target.getClass(), e);
            }
        }
    }

    protected Object getTargetValue(Object target, Field field) {
        try {
            if (field != null) {
                field.setAccessible(true);
                return field.get(target);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to obtain value from field" + field.getName() + ", class: " + target.getClass(), e);
        }
        return null;
    }

    private static class ObjectIdSelector {

        public boolean canBeConsideredAsObjectId(Field f) {
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
}
