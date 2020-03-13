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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.ReflectiveObjectIdUpdater;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.lang.reflect.Field;

/**
 * Use {@link JacksonObjectIdUpdater} instead
 */
@Deprecated
public class JacksonIdFieldSelector implements ReflectiveObjectIdUpdater.IdFieldSelector {

    public boolean isId(Field f) {
        return has_IdName(f) || hasJsonProperty(f) || hasIdAnnotation(f);
    }

    public boolean isObjectId(Field f) {
        return f.isAnnotationPresent(org.jongo.marshall.jackson.oid.ObjectId.class)
                || f.isAnnotationPresent(MongoObjectId.class)
                || ObjectId.class.isAssignableFrom(f.getType());
    }

    private boolean has_IdName(Field f) {
        return "_id".equals(f.getName());
    }

    private boolean hasJsonProperty(Field f) {
        JsonProperty annotation = f.getAnnotation(JsonProperty.class);
        return annotation != null && "_id".equals(annotation.value());
    }

    private boolean hasIdAnnotation(Field f) {
        Id id = f.getAnnotation(Id.class);
        MongoId mongoId = f.getAnnotation(MongoId.class);
        return id != null || mongoId != null;
    }
}
