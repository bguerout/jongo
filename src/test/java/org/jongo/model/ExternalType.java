package org.jongo.model;

import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import org.jongo.marshall.jackson.oid.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models a type coming from a third party tool like JsonSchema2Pojo.
 * 
 * @author Christian Trimble
 */
@SuppressWarnings("deprecation")
public class ExternalType {
    /**
     * Mixin that supplies all of the mongo specific annotations.
     * 
     * @author Christian Trimble
     */
    public static abstract class ExternalTypeMixin {
        @MongoObjectId
        @MongoId
        @ObjectId
        @Id
        public String id;

        @MongoObjectId
        @MongoId
        @ObjectId
        @Id
        public abstract String getId();

        @MongoObjectId
        @MongoId
        @ObjectId
        @Id
        public abstract void setId( String id );
    }

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    public ExternalType() {}

    public ExternalType( String name ) {
        this.name = name;
    }

    public ExternalType( String id, String name ) {
        this.id = id;
        this.name = name;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
}
