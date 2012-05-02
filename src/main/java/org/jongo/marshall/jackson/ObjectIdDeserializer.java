package org.jongo.marshall.jackson;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
        String id = jp.getText();
        if (id.startsWith("{")) {
            return createObjectIdFromAField(jp, id);
        }
        return new ObjectId(id);
    }

    private ObjectId createObjectIdFromAField(JsonParser jp, String id) throws IOException {
        JsonNode oid = jp.readValueAsTree().get("$oid");
        if (oid == null) {
            throw new IllegalArgumentException("Unable to convert " + id + " into an ObjectId");
        }
        return new ObjectId(oid.getTextValue());
    }
}
