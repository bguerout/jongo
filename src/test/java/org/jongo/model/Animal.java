package org.jongo.model;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "_class")
public class Animal {
    String name;

    Animal() {
    }

    public Animal(String name) {
        this.name = name;
    }
}
