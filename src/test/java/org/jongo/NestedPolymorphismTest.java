package org.jongo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NestedPolymorphismTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("owners");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("owners");
    }

    @Test
    public void testCanSaveAndReadNestedPolymorphicField() throws Exception {
        Owner owner = new Owner();
        owner.setPet(new Cat("Sylvester", 42));

        collection.save(owner);

        Owner owner2 = collection.findOne(owner.getId()).as(Owner.class);
        assertThat(owner2.getPet()).isInstanceOf(Cat.class);

        Cat cat = (Cat) owner2.getPet();
        assertThat(cat.getName()).isEqualTo("Sylvester");
        assertThat(cat.getMiceEaten()).isEqualTo(42);
    }

    @Test
    public void testCanUpdatePolymorphicField() throws Exception {

        Owner owner = new Owner();
        owner.setPet(new Cat("Sylvester", 42));

        collection.save(owner);

        collection.update("{_id: #}", owner.getId()).with("{$set: {pet: #}}", new Cat("Tom", 0));

        Owner owner2 = collection.findOne(owner.getId()).as(Owner.class);
        Cat cat = (Cat) owner2.getPet();
        assertThat(cat.getName()).isEqualTo("Tom");
        assertThat(cat.getMiceEaten()).isEqualTo(0);

    }

    public static class Owner {
        @Id
        @MongoId //see NewAnnotationsCompatibilitySuiteTest for more informations
        private ObjectId id;

        private Pet pet;

        public ObjectId getId() {
            return id;
        }

        public Pet getPet() {
            return pet;
        }

        public void setPet(Pet pet) {
            this.pet = pet;
        }
    }

    /**
     * A Pet is serialized with a "type" attribute, which is more robust
     * and cross-language than the default @class attribute
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(name = "cat", value = Cat.class)})
    public static class Pet {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Cat extends Pet {
        private int miceEaten;

        public Cat() {
        }

        public Cat(String name, int mice) {
            setName(name);
            setMiceEaten(mice);
        }

        public int getMiceEaten() {
            return miceEaten;
        }

        public void setMiceEaten(int miceEaten) {
            this.miceEaten = miceEaten;
        }
    }
}
