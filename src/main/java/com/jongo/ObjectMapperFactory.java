package com.jongo;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_DEFAULT;

public class ObjectMapperFactory {

    public static ObjectMapper createConfLessMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().without(FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.setSerializationConfig(mapper.getSerializationConfig().withSerializationInclusion(NON_DEFAULT));
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
        return mapper;
    }
}
