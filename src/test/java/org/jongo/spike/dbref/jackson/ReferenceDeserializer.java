package org.jongo.spike.dbref.jackson;

import com.mongodb.DB;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class ReferenceDeserializer extends JsonDeserializer<Reference> {

    private ObjectMapper mapper;
    private DB db;

    public ReferenceDeserializer(ObjectMapper mapper, DB db) {
        this.mapper = mapper;
        this.db = db;
    }

    @Override
    public Reference deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String json = parser.readValueAsTree().toString();
        return new Reference(convertToDBRef(json), mapper);
    }

    private DBRef convertToDBRef(String json) {
        DBRef dbRef = (DBRef) JSON.parse(json);
        return new DBRef(db, dbRef.getRef(), dbRef.getId());
    }

}
