package org.jongo.marshall;

import com.mongodb.DBObject;

public class QueryMarshaller {

    public QueryMarshaller(Marshaller<Object, String> parameterMarshaller, Marshaller<String, DBObject> queryMarshaller) {
        this.parameterMarshaller = parameterMarshaller;
        this.queryMarshaller = queryMarshaller;
    }

    Marshaller<Object, String> parameterMarshaller;

    Marshaller<String, DBObject> queryMarshaller;

    public Marshaller<Object, String> getParameterMarshaller() {
        return parameterMarshaller;
    }

    public Marshaller<String, DBObject> getQueryMarshaller() {
        return queryMarshaller;
    }
}
