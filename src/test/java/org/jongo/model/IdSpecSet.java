package org.jongo.model;

import java.lang.reflect.Field;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import com.fasterxml.jackson.annotation.*;

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

    public static <T> T id( T specInstance, Object id ) {
        Field field = idField(specInstance.getClass());
        try {
            field.setAccessible(true);
            if( ObjectId.class.isAssignableFrom(field.getType()) ) {
                field.set(specInstance,  (ObjectId)id);
            }
            else {
                field.set(specInstance, id.toString());
            }
            return specInstance;
        } catch( Exception e ) {
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

    public static Field idField( Class<?> spec ) {
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
