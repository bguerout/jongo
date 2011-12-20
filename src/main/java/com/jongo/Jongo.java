package com.jongo;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class Jongo
{
    private static ObjectMapper mapper;

    static
    {
        mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(Std.defaultInstance().withFieldVisibility(ANY));
    }

    static DBObject createQuery(String query)
    {
        return ((DBObject) JSON.parse(query));
    }

    static String marshallQuery(Object obj) throws IOException
    {
        Writer writer = new StringWriter();
        mapper.writeValue(writer, obj);
        return writer.toString();
    }

    static DBObject marshall(Object obj) throws IOException
    {
        Writer writer = new StringWriter();
        mapper.writeValue(writer, obj);
        return createQuery(writer.toString());
    }

    public static <T> T unmarshallString(String json, Class<T> clazz) throws IOException
    {
        return mapper.readValue(json, clazz);
    }
}
