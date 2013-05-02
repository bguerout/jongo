package org.jongo;

import com.mongodb.DBObject;

public class RawResultHandler<T extends DBObject> implements ResultHandler<T> {
    public static <T extends DBObject> RawResultHandler<T> asRaw(Class<T> clazz) {
        return new RawResultHandler<T>(clazz);
    }

    private final Class<T> clazz;

    public RawResultHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T map(DBObject result) {
        return clazz.cast(result);
    }
}
