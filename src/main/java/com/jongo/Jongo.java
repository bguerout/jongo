package com.jongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Jongo
{
    private static ObjectMapper mapper = new ObjectMapper();

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
