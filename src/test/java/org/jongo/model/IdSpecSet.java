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

package org.jongo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.lang.reflect.Field;

/**
 * A set of example id specifications.
 *
 * @author Christian Trimble
 */
public class IdSpecSet {
    public static class BrokenStringIdField {
        public String id;
    }

    public static class StringIdBare {
        public String _id;
    }

    public static class StringIdJsonProperty {
        @JsonProperty("_id")
        public String id;
    }

    public static class BrokenStringIdJsonProperty {
        @JsonProperty("id")
        public String id;
    }

    public static class StringIdMongoId {
        @MongoId
        public String id;
    }

    public static class StringIdMongoObjectId {
        @MongoObjectId
        public String _id;
    }

    public static class StringIdMongoIdMongoObjectId {
        @MongoId
        @MongoObjectId
        public String id;
    }

    public static class ObjectIdBare {
        public ObjectId _id;
    }

    public static class ObjectIdJsonProperty {
        @JsonProperty("_id")
        public ObjectId id;
    }

    public static class ObjectIdMongoId {
        @MongoId
        public ObjectId id;
    }

    public static class ObjectIdMongoObjectId {
        @MongoObjectId
        public ObjectId _id;
    }

    public static class ObjectIdMongoIdMongoObjectId {
        @MongoId
        @MongoObjectId
        public ObjectId id;
    }

    public static abstract class StringIdMongoIdMixIn {
        @MongoId
        public String id;
    }

    public static abstract class String_IdMongoObjectIdMixIn {
        @MongoObjectId
        public String _id;
    }

    public static abstract class StringIdMongoObjectIdMixIn {
        @MongoObjectId
        public String id;
    }

    public static abstract class StringIdMongoIdMongoObjectIdMixIn {
        @MongoId
        @MongoObjectId
        public String id;
    }

    public static <T> T id(T specInstance, Object id) {
        Field field = idField(specInstance.getClass());
        try {
            field.setAccessible(true);
            if (ObjectId.class.isAssignableFrom(field.getType())) {
                field.set(specInstance, (ObjectId) id);
            } else {
                field.set(specInstance, id.toString());
            }
            return specInstance;
        } catch (Exception e) {
            throw new RuntimeException("could not set id field", e);
        }
    }

    public static Object id(Object specInstance) {
        Field field = idField(specInstance.getClass());

        field.setAccessible(true);
        try {
            return field.get(specInstance);
        } catch (Exception e) {
            throw new RuntimeException("could not set id", e);
        }
    }

    public static <T> T newInstanceWithId(Class<T> spec, ObjectId id) {
        try {
            return id(spec.newInstance(), id);
        } catch (Exception e) {
            throw new RuntimeException("could not create spec instance", e);
        }
    }

    public static Field idField(Class<?> spec) {
        try {
            return spec.getDeclaredField("_id");
        } catch (NoSuchFieldException e) {
            try {
                return spec.getDeclaredField("id");
            } catch (NoSuchFieldException e1) {
                throw new RuntimeException("_id field missing", e1);
            }
        }
    }
}
