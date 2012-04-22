package org.jongo.spike.dbref;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jongo.spike.dbref.jackson.DBRefDeserializer;
import org.jongo.spike.dbref.jackson.DBRefSerializer;

class Buddy {
    @JsonProperty("_id")
    ObjectId id;
    String name;
    @JsonDeserialize(using = DBRefDeserializer.class)
    @JsonSerialize(using = DBRefSerializer.class)
    Buddy friend;

    public Buddy(String name, Buddy friend) {
        this.friend = friend;
        this.name = name;
    }

    Buddy() {
    }
}
