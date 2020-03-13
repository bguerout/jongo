/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

package org.jongo;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.IdSelector;
import org.jongo.marshall.jackson.JacksonObjectIdUpdater;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Use {@link JacksonObjectIdUpdater} instead
 */
@Deprecated
public class ReflectiveObjectIdUpdater implements ObjectIdUpdater {

    private final Map<Class<?>, Field> fieldCache = new HashMap<Class<?>, Field>();
    private final IdFieldSelector idFieldSelector;

    public ReflectiveObjectIdUpdater(IdFieldSelector idFieldSelector) {
        this.idFieldSelector = idFieldSelector;
    }

    public boolean mustGenerateObjectId(Object pojo) {
        Field idField = selectIdField(pojo.getClass());
        return idField != null && isAnEmptyObjectId(pojo, idField);
    }

    public Object getId(Object pojo) {
        Field idField = selectIdField(pojo.getClass());
        if (idField != null) {
            try {
                idField.setAccessible(true);
                Object id = idField.get(pojo);

                if (id instanceof String && idFieldSelector.isObjectId(idField)) {
                    return new ObjectId(id.toString());
                } else {
                    return id;
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to obtain objectid from field" + idField.getName() + ", class: " + idField.getClass(), e);
            }
        }
        return null;
    }

    public void setObjectId(Object newPojo, ObjectId id) {
        Field idField = selectIdField(newPojo.getClass());
        if (idField == null) {
            return;
        } else if (!mustGenerateObjectId(newPojo)) {
            throw new IllegalArgumentException("Unable to set objectid on class: " + newPojo.getClass());
        }
        updateField(newPojo, id, idField);
    }

    private void updateField(Object target, ObjectId id, Field field) {
        try {
            if (field.getType().equals(ObjectId.class)) {
                field.setAccessible(true);
                field.set(target, id);
            } else if (field.getType().equals(String.class) && idFieldSelector.isObjectId(field)) {
                field.setAccessible(true);
                field.set(target, id.toString());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to set objectid on class: " + target.getClass(), e);
        }
    }

    private Field selectIdField(Class<?> clazz) {
        if (fieldCache.containsKey(clazz)) {
            return fieldCache.get(clazz);
        }

        Class<?> c = clazz;
        while (!Object.class.equals(c)) {
            for (Field f : c.getDeclaredFields()) {
                if (idFieldSelector.isId(f)) {
                    fieldCache.put(clazz, f);
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private boolean isAnEmptyObjectId(Object target, Field field) {
        if (idFieldSelector.isObjectId(field)) {
            try {
                field.setAccessible(true);
                return field.get(target) == null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to obtain value from field" + field.getName() + ", class: " + target.getClass(), e);
            }
        }
        return false;
    }

    @Deprecated
    public interface IdFieldSelector extends IdSelector<Field>{
        boolean isId(Field f);

        boolean isObjectId(Field f);
    }
}
