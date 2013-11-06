/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jongo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jongo.model.Animal;
import org.jongo.model.Fox;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PolymorphismTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void canFindInheritedEntity() throws IOException {
        collection.save(new Fox("fantastic", "roux"));

        Animal animal = collection.findOne("{name:'fantastic'}").as(Animal.class);

        assertThat(animal).isInstanceOf(Fox.class);
        assertThat(animal.getId()).isNotNull();
    }

    @Test
    public void canUpdateIdFieldOnSuperType() throws IOException {

        Fox fox = new Fox("fantastic", "roux");

        collection.save(fox);

        Animal result = collection.findOne().as(Fox.class);
        assertThat(fox.getId()).isNotNull();
        assertThat(result.getId()).isEqualTo(fox.getId());
    }

    @Test
    public void canFindInheritedEntityByDiscriminator() throws IOException {

        collection.insert("{name:'piou piout', discriminator:'L'}");
        collection.insert("{name:'hunter', discriminator:'B'}");

        Dog dog = collection.findOne("{name:'hunter'}").as(Dog.class);

        assertThat(dog).isInstanceOf(Beagle.class);
        assertThat(dog.name).isEqualTo("hunter");
        assertThat(dog.discriminator).isEqualTo("B");
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "discriminator",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Beagle.class, name = "B"),
            @JsonSubTypes.Type(value = Loulou.class, name = "L")
    })
    private static abstract class Dog {
        String name, discriminator;
    }

    private static class Beagle extends Dog {
    }

    private static class Loulou extends Dog {
    }


}
