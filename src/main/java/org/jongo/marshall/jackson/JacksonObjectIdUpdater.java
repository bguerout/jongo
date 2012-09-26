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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

class JacksonObjectIdUpdater implements ObjectIdUpdater {

    private final Map<Class<?>, Field> idFields = new HashMap<Class<?>, Field>();

    public void setDocumentGeneratedId(Object target, ObjectId id) {
        Field field = findFieldOrNull(target.getClass());
        if (field != null) {
            updateField(target, id, field);
        }
    }

    protected void updateField(Object target, Object id, Field field) {
        try {

            field.setAccessible(true);
            if (field.get(target) == null) {
                field.set(target, id);
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to set objectid on class: " + target.getClass(), e);
        }
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
                if (f.getType().equals(ObjectId.class)) {
                    if (isId(f.getName()) || isAnnotated(f)) {
                        idFields.put(clazz, f);
                        return f;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

    private boolean isAnnotated(Field f) {
        JsonProperty annotation = f.getAnnotation(JsonProperty.class);
        return annotation != null && isId(annotation.value());
    }

    private boolean isId(String value) {
        return "_id".equals(value);
    }
}
