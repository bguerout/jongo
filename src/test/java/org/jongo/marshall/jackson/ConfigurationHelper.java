package org.jongo.marshall.jackson;


public class ConfigurationHelper {

    public static Mapping mapping() {
        return mapping(new JacksonMapper.Builder());
    }
    
    public static Mapping mapping(JacksonMapper.Builder builder) {
        return builder.innerMapping();
    }
}
