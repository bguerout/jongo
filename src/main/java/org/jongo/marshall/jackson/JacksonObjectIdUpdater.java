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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.bson.types.ObjectId;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

/**
 * An ObjectIdUpdater based on Jackson's view of on object.
 *
 * @author Christian Trimble
 */
@SuppressWarnings("deprecation")
public class JacksonObjectIdUpdater implements ObjectIdUpdater {

    private final ObjectMapper mapper;
    private final IdSelector<BeanPropertyDefinition> idSelector;

    public JacksonObjectIdUpdater(ObjectMapper mapper) {
        this(mapper, new BeanPropertyDefinitionIdSelector());
    }

    public JacksonObjectIdUpdater(ObjectMapper mapper, IdSelector<BeanPropertyDefinition> idSelector) {
        this.mapper = mapper;
        this.idSelector = idSelector;
    }

    public boolean mustGenerateObjectId(Object pojo) {
        for (BeanPropertyDefinition def : beanDescription(pojo.getClass()).findProperties()) {
            if (idSelector.isId(def)) {
                AnnotatedMember accessor = def.getAccessor();
                accessor.fixAccess(true);
                return idSelector.isObjectId(def) && accessor.getValue(pojo) == null;
            }
        }
        return false;
    }

    public Object getId(Object pojo) {
        BasicBeanDescription beanDescription = beanDescription(pojo.getClass());
        for (BeanPropertyDefinition def : beanDescription.findProperties()) {
            if (idSelector.isId(def)) {
                AnnotatedMember accessor = def.getAccessor();
                accessor.fixAccess(true);
                Object id = accessor.getValue(pojo);
                if (id instanceof String && idSelector.isObjectId(def)) {
                    return new ObjectId(id.toString());
                } else {
                    return id;
                }
            }
        }
        return null;
    }

    public void setObjectId(Object target, ObjectId id) {
        for (BeanPropertyDefinition def : beanDescription(target.getClass()).findProperties()) {
            if (idSelector.isId(def)) {
                AnnotatedMember accessor = def.getAccessor();
                accessor.fixAccess(true);
                if (accessor.getValue(target) != null) {
                    throw new IllegalArgumentException("Unable to set objectid on class: " + target.getClass());
                }
                AnnotatedMember field = def.getField();
                field.fixAccess(true);
                Class<?> type = field.getRawType();
                if (ObjectId.class.isAssignableFrom(type)) {
                    field.setValue(target, id);
                } else if (type.equals(String.class) && idSelector.isObjectId(def)) {
                    field.setValue(target, id.toString());
                }
                return;
            }
        }
    }

    private BasicBeanDescription beanDescription(Class<?> cls) {
        BasicClassIntrospector bci = new BasicClassIntrospector();
        return bci.forSerialization(mapper.getSerializationConfig(), mapper.constructType(cls), mapper.getSerializationConfig());
    }

    public static class BeanPropertyDefinitionIdSelector implements IdSelector<BeanPropertyDefinition> {

        public boolean isId(BeanPropertyDefinition property) {
            return hasIdName(property) || hasIdAnnotation(property);
        }

        public boolean isObjectId(BeanPropertyDefinition property) {
            return property.getPrimaryMember().getAnnotation(org.jongo.marshall.jackson.oid.ObjectId.class) != null
                    || property.getPrimaryMember().getAnnotation(MongoObjectId.class) != null
                    || ObjectId.class.isAssignableFrom(property.getAccessor().getRawType());
        }

        private static boolean hasIdName(BeanPropertyDefinition property) {
            return "_id".equals(property.getName());
        }

        private static boolean hasIdAnnotation(BeanPropertyDefinition property) {
            if (property == null) return false;
            AnnotatedMember accessor = property.getPrimaryMember();
            return accessor != null && (accessor.getAnnotation(MongoId.class) != null || accessor.getAnnotation(Id.class) != null);
        }
    }
}
