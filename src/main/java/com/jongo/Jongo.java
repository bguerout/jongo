package com.jongo;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_DEFAULT;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import com.mongodb.DBObject;

public class Jongo
{
    private static ObjectMapper mapper;

    static
    {
        mapper = new ObjectMapper();
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().without(FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(NON_DEFAULT));
        mapper.setVisibilityChecker(Std.defaultInstance().withFieldVisibility(ANY));
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
        return DBObjectConvertor.from(writer.toString());
    }

    public static <T> T unmarshallString(String json, Class<T> clazz) throws IOException
    {
        return mapper.readValue(json, clazz);
    }
}
