package com.jongo;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class JsonMapper {

    private final ObjectMapper mapper;

    public JsonMapper() {
        this.mapper = ObjectMapperFactory.createConfLessMapper();
    }


    public <T> T getEntity(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }
}
