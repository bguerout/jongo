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

package org.jongo.marshall.jackson;

import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.util.JongoEmbeddedRule;
import org.jongo.util.MongoEmbeddedRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.jongo.model.IdSpecSet.*;
import static org.junit.Assert.assertThat;

/**
 * Tests how Jongo handles different field/annotation/mixin combinations.
 *
 * @author Christian Trimble
 */
@RunWith(Parameterized.class)
public class IdSpecTest {
    @Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {StringIdBare.class, noMixIn(), StringIdBare.class},
                {BrokenStringIdField.class, StringIdMongoIdMixIn.class, StringIdBare.class},
                {BrokenStringIdField.class, StringIdMongoIdMongoObjectIdMixIn.class, ObjectIdBare.class},
                {StringIdBare.class, String_IdMongoObjectIdMixIn.class, ObjectIdBare.class},
                {StringIdJsonProperty.class, noMixIn(), StringIdBare.class},
                {BrokenStringIdJsonProperty.class, StringIdMongoIdMixIn.class, StringIdBare.class},
                {StringIdMongoId.class, noMixIn(), StringIdBare.class},
                {StringIdMongoObjectId.class, noMixIn(), ObjectIdBare.class},
                {StringIdMongoIdMongoObjectId.class, noMixIn(), ObjectIdBare.class},
                {ObjectIdBare.class, noMixIn(), ObjectIdBare.class},
                {ObjectIdJsonProperty.class, noMixIn(), ObjectIdBare.class},
                {ObjectIdMongoId.class, noMixIn(), ObjectIdBare.class},
                {ObjectIdMongoObjectId.class, noMixIn(), ObjectIdBare.class},
                {ObjectIdMongoIdMongoObjectId.class, noMixIn(), ObjectIdBare.class}
        });
    }

    public static
    @ClassRule
    MongoEmbeddedRule mongoRule = new MongoEmbeddedRule();
    public
    @Rule
    JongoEmbeddedRule jongoRule = new JongoEmbeddedRule(mongoRule);

    private Class<?> spec;
    private Class<?> equiv;
    private Class<?> mixIn;
    private MongoCollection collection;

    public IdSpecTest(Class<?> spec, Class<?> mixIn, Class<?> equiv) {
        this.spec = spec;
        this.equiv = equiv;
        this.mixIn = mixIn;
        if (this.mixIn != null) {
            jongoRule.withMixIn(spec, this.mixIn);
        }
    }

    @Before
    public void setUp() throws UnknownHostException {
        this.collection = jongoRule.createEmptyCollection("spec");
    }

    @Test
    public void saveAndFind() {
        collection.drop();
        Object instance = newInstanceWithId(spec, new ObjectId());
        WriteResult saveResult = collection.save(instance);
        assertThat(saveResult.getN(), equalTo(1));

        MongoCursor<?> found = collection.find("{_id: #}", mongoId(instance)).as(spec);

        assertThat(found.count(), equalTo(1));
        assertThat(id(found.next()), equalTo(id(instance)));
    }

    @Test
    public void marshalledHasIdField() {
        Object instance = newInstanceWithId(spec, new ObjectId());
        org.jongo.bson.BsonDocument document = jongoRule.getMapper().getMarshaller().marshall(instance);
        assertThat(document.toDBObject().containsField("_id"), equalTo(true));
    }

    @Test
    public void marshalledHasCorrectIdType() {
        Object instance = newInstanceWithId(spec, new ObjectId());
        org.jongo.bson.BsonDocument document = jongoRule.getMapper().getMarshaller().marshall(instance);
        Object id = mongoId(instance);
        assertThat(document.toDBObject().get("_id"), instanceOf(id.getClass()));
    }

    @Test
    public void marchalRoundTrip() {
        Object instance = newInstanceWithId(spec, new ObjectId());
        org.jongo.bson.BsonDocument document = jongoRule.getMapper().getMarshaller().marshall(instance);
        Object roundTrip = jongoRule.getMapper().getUnmarshaller().unmarshall(document, spec);
        assertThat(id(roundTrip), equalTo(id(instance)));
    }

    public Object mongoId(Object instance) {
        Object id = id(instance);
        if (String.class.isAssignableFrom(id.getClass())) {
            if (equiv == StringIdBare.class) {
                return id;
            } else if (equiv == ObjectIdBare.class) {
                return new ObjectId((String) id);
            }
        } else if (ObjectId.class.isAssignableFrom(id.getClass())) {
            if (equiv == StringIdBare.class) {
                return id.toString();
            } else if (equiv == ObjectIdBare.class) {
                return id;
            }
        }
        throw new IllegalStateException("cannot build mongo id");
    }

    private static Class<?> noMixIn() {
        return null;
    }
}
