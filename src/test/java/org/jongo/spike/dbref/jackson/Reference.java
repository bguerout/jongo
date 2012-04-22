package org.jongo.spike.dbref.jackson;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import org.codehaus.jackson.map.ObjectMapper;
import org.jongo.Jongo;

import java.io.IOException;

public class Reference {

    private final DBRef dbRef;
    private final ObjectMapper mapper;

    public Reference(DBRef dbRef, ObjectMapper mapper) {
        this.dbRef = dbRef;
        this.mapper = mapper;
    }

    public <T> T as(Class<T> rawClass) throws IOException {
        DBObject dbObject = dbRef.fetch();
        return mapper.readValue(Jongo.toJson(dbObject), rawClass);
    }

    public DBRef getDbRef() {
        return dbRef;
    }
}
