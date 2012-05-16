package org.jongo.marshall;

import com.mongodb.util.JSON;

public class NativeMarshaller implements Marshaller {

    public <T> String marshall(T obj) {
        return JSON.serialize(obj);
    }

}
