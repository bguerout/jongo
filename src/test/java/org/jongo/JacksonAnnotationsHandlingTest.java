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

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonAnnotationsHandlingTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        collection.drop();
    }

    @Test
    public void canHandleObjectIdNotAnnotated() throws Exception {

        // given
        POJONotAnnotated pojo = new POJONotAnnotated();
        pojo._id = new ObjectId("4f92d1ae44ae2dac4527d49b");

        // when
        collection.save(pojo);

        // then
        POJONotAnnotated result = collection.findOne().as(POJONotAnnotated.class);
        assertThat(result._id.toString()).isEqualTo("4f92d1ae44ae2dac4527d49b");
    }

    @Test
    public void canHandleStringIdNotAnnotated() throws Exception {

        // given
        StringNotAnnotated pojo = new StringNotAnnotated();
        pojo._id = 1500L;

        // when
        collection.save(pojo);

        // then
        StringNotAnnotated result = collection.findOne().as(StringNotAnnotated.class);
        assertThat(result._id).isEqualTo(1500L);
    }

    @Test
    public void canInjectObjectIdIntoAMisspelledGetter() throws Exception {

        // given
        POJOWithMisspelledGetter pojo = new POJOWithMisspelledGetter();
        ObjectId id = new ObjectId("4f92d1ae44ae2dac4527d49b");
        pojo.setAnotherName(id);

        // when
        collection.save(pojo);

        // then
        POJOWithMisspelledGetter result = collection.findOne().as(POJOWithMisspelledGetter.class);
        assertThat(result.getAnotherName()).isEqualTo(id);
        assertThat(result._id).isEqualTo(id);
    }

    @Test
    public void canHandleAnnotatedGetter() throws Exception {
        POJOWithAnnotatedGetter pojo = new POJOWithAnnotatedGetter();
        pojo.setId("id");

        collection.save(pojo);

        POJOWithAnnotatedGetter result = collection.findOne().as(POJOWithAnnotatedGetter.class);
        assertThat(result.getId()).isEqualTo(pojo.getId());
    }

    public static class POJOWithMisspelledGetter {

        private ObjectId _id;

        public ObjectId getAnotherName() {
            return _id;
        }

        public void setAnotherName(ObjectId _id) {
            this._id = _id;
        }
    }

    public static class POJONotAnnotated {

        private ObjectId _id;

    }

    public static class StringNotAnnotated {

        private long _id;

    }

    public static class POJOWithAnnotatedGetter {
        private String someId;

        @MongoId
        public String getId() {
            return someId;
        }

        public void setId(String id) {
            someId = id;
        }
    }

}
