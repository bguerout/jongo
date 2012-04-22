package org.jongo.spike.dbref.jackson;

import com.mongodb.DBRef;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReferenceSerializer extends JsonSerializer<Object> {

    private Map<Class<?>, ReferenceLink<?>> referenceLinks = new HashMap<Class<?>, ReferenceLink<?>>();

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

        if (!referenceLinks.containsKey(value.getClass())) {
            throw new IllegalArgumentException("Unable to serialize " + value.getClass() + ", no translators has been defined.");
        }

        ReferenceLink link = referenceLinks.get(value.getClass());
        String json = new DBRef(null, link.getReferenceCollectionName(value), getId(value, link)).toString();
        jgen.writeRawValue(json);
    }

    private String getId(Object value, ReferenceLink link) {

        String id = link.getId(value);
        if (id == null) {
            throw new NullPointerException("Cannot create DRRef because its id is null into " + value);
        }
        return id;
    }

    public void registerReferenceLink(Class<?> typeClass, ReferenceLink<?> link) {
        referenceLinks.put(typeClass, link);
    }

}
