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

package org.jongo;

import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ReflectiveObjectIdUpdater implements ObjectIdUpdater {

    private final Map<Class<?>, Field> idFields = new HashMap<Class<?>, Field>();
    private final IdFieldSelector idFieldSelector;

    public ReflectiveObjectIdUpdater(IdFieldSelector idFieldSelector) {
        this.idFieldSelector = idFieldSelector;
    }

    public boolean canSetObjectId(Object target) {
        Field oidField = findFieldOrNull(target.getClass());
        return oidField != null && getIdFieldValue(target, oidField) == null;
    }

    public void setObjectId(Object target, ObjectId id) {
        Field field = findFieldOrNull(target.getClass());
        if (field == null) {
            throw handleInvalidTarget(target);
        }
        updateField(target, id, field);
    }

    protected void updateField(Object target, ObjectId id, Field field) {
        Object value = getIdFieldValue(target, field);
        if (value == null) {
            try {
                if (field.getType().equals(ObjectId.class)) {
                    field.set(target, id);
                } else if (field.getType().equals(String.class)) {
                    field.set(target, id.toString());
                }
            } catch (IllegalAccessException e) {
                throw handleInvalidTarget(target);
            }
        }
    }

    private RuntimeException handleInvalidTarget(Object target) {
        return new IllegalArgumentException("Unable to set objectid on class: " + target.getClass());
    }

    private Field findFieldOrNull(Class<?> clazz) {
        if (idFields.containsKey(clazz)) {
            return idFields.get(clazz);
        }

        while (!Object.class.equals(clazz)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            if (declaredFields == null) {
                return null;
            }
            for (Field f : declaredFields) {
                if (idFieldSelector.isId(f) && isObjectIdCompliant(f.getType())) {
                    idFields.put(clazz, f);
                    return f;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private boolean isObjectIdCompliant(Class<?> type) {
        return type.equals(ObjectId.class) || type.equals(String.class);
    }

    private Object getIdFieldValue(Object target, Field field) {
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

    public interface IdFieldSelector {
        public boolean isId(Field f);
    }

}
