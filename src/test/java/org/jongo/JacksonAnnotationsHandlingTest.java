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

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.JongoTest.collection;

import org.bson.types.ObjectId;
import org.junit.Rule;
import org.junit.Test;

public class JacksonAnnotationsHandlingTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("users");

    @Test
    public void canHandleObjectIdNotAnnotated() throws Exception {

        // given
        POJONotAnnotated pojo = new POJONotAnnotated();
        pojo._id = new ObjectId("4f92d1ae44ae2dac4527d49b");

        // when
        String id = collection.save(pojo);

        // then
        POJONotAnnotated result = collection.findOne("{}").as(POJONotAnnotated.class);
        assertThat(result._id).isEqualTo(id);
    }

    @Test
    public void canHandleStringIdNotAnnotated() throws Exception {

        // given
        StringNotAnnotated pojo = new StringNotAnnotated();
        pojo._id = 1500L;

        // when
        collection.save(pojo);

        // then
        StringNotAnnotated result = collection.findOne("{}").as(StringNotAnnotated.class);
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
        POJOWithMisspelledGetter result = collection.findOne("{}").as(POJOWithMisspelledGetter.class);
        assertThat(result.getAnotherName()).isEqualTo(id);
        assertThat(result._id).isEqualTo(id);
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
}
