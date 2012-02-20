package org.jongo.model;

public class Fox extends Animal {
    String color;

    Fox() {
    }

    public Fox(String name, String color) {
        super(name);
        this.color = color;
    }
}
