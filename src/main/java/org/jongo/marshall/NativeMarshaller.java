package org.jongo.marshall;

import com.mongodb.util.JSON;
import org.jongo.BSONPrimitives;

public class NativeMarshaller implements Marshaller {

    public <T> String marshall(T obj) {
        return JSON.serialize(obj);
    }

    public boolean supports(Object obj) {
        return BSONPrimitives.contains(obj.getClass());
    }

}
