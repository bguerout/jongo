/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bson.types.BSONTimestamp;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.model.Animal;
import org.jongo.model.Fox;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PolymorphismTest extends JongoTestBase {

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

    @Test
    public void canHandleInheritanceInASubDocument() throws Exception {

        collection.save(new Zoo("Vincennes", new Fox("Zorro", "roux")));

        Zoo zoo = collection.findOne().as(Zoo.class);
        assertThat(zoo).isNotNull();
        assertThat(zoo.mascot.getName()).isEqualTo("Zorro");
    }

    @Test
    //https://github.com/bguerout/jongo/issues/258
    public void canHandleInheritanceAsAQueryParameter() throws Exception {
        Chiwawa chiwawa = new Chiwawa();
        collection.insert(chiwawa);

        collection.update(chiwawa._id).with("#", chiwawa);

        Chiwawa result = collection.findOne().as(Chiwawa.class);
        assertThat(result).isNotNull();
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "discriminator",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Beagle.class, name = "B"),
            @JsonSubTypes.Type(value = Loulou.class, name = "L"),
            @JsonSubTypes.Type(value = Chiwawa.class, name = "C")
    })
    private static abstract class Dog {
        ObjectId _id;
        String name, discriminator;
    }

    private static class Beagle extends Dog {
    }

    private static class Loulou extends Dog {
    }

    private static class Chiwawa extends Dog {
        private String longText = "chiwawa-chiwawa-chiwawa-chiwawa-chiwawachiwawachiwawachiwawachiwawachiwawa";
    }

    private static class Zoo {

        private String name;
        private Animal mascot;

        private Zoo() {
            //jackson
        }

        public Zoo(String name, Animal mascot) {
            this.name = name;
            this.mascot = mascot;
        }
    }

    @Test
    public void canFindInheritedWithBSONTimestamp() throws IOException {
        BsonTypes entity = new BsonTypes();
        entity._id = new BSONTimestamp(((int) System.currentTimeMillis() / 1000), 0);
        entity.value = new BSONTimestamp(((int) System.currentTimeMillis() / 1000), 0);
        collection.save(entity);

        BsonTypes found = collection.findOne().as(BsonTypes.class);

        //assertThat(found).isInstanceOf(TimestampType.class);
        assertThat(found._id).isEqualTo(entity._id);
        assertThat(found.value).isEqualTo(entity.value);
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "discriminator",
            visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TimestampType.class, name = "timestamp"),
            @JsonSubTypes.Type(value = MinKeyType.class, name = "minKey"),
    })
    private static class BsonTypes {
        @MongoId
        @JsonProperty("_id")
        public BSONTimestamp _id;
        public BSONTimestamp value;

        public BSONTimestamp get_Id() {
            return _id;
        }

        @JsonProperty
        public BSONTimestamp getValue() {
            return value;
        }

        @JsonProperty
        public String discriminator;
    }

    private static class TimestampType extends BsonTypes {

    }

    private static class MinKeyType {
        @JsonProperty
        public MinKey value;
    }


}
