package com.jongo;

import org.codehaus.jackson.annotate.JsonProperty;

public class Poi
{
    @JsonProperty
    String address;

    public Poi() {

    }

    public Poi(@JsonProperty("address") String address)
    {
        this.address = address;
    }
}
