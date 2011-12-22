package com.jongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class DBObjectConvertor {

    private final ObjectMapper mapper;

    public DBObjectConvertor() {
        this.mapper = ObjectMapperFactory.createConfLessMapper();
    }

    public DBObject convert(String jsonQuery) {
        return ((DBObject) JSON.parse(jsonQuery));
    }

    public DBObject convert(Object obj) throws IOException {
        Writer writer = new StringWriter();
        mapper.writeValue(writer, obj);
        return convert(writer.toString());
    }
}
