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

package org.jongo.marshall.jackson;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.util.JongoTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class UnmarshallingWithJsonCreatorTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("marshalling");
    }

    @Test
    public void canSaveAndMapAPojoWithoutId() throws Exception {

        collection.save(new NoId("test"));

        NoId noId = collection.findOne().as(NoId.class);

        assertThat(noId.value).isEqualTo("test");
    }

    private static class NoId {
        final String value;

        @JsonCreator
        public NoId(@JsonProperty("value") String value) {
            this.value = value;
        }
    }

    @Test
    public void canSaveAndMapAPojoStringCreatorAndObjectId() throws Exception {

        ObjectId id = new ObjectId();
        collection.save(new StringCreatorAndObjectId("test").withObjectId(id));

        StringCreatorAndObjectId noId = collection.findOne().as(StringCreatorAndObjectId.class);

        assertThat(noId.value).isEqualTo("test");
        assertThat(noId.getId()).isEqualTo(id);
    }

    private static class StringCreatorAndObjectId {
        protected ObjectId id;
        final String value;

        @JsonCreator
        public StringCreatorAndObjectId(@JsonProperty("value") String value) {
            this.value = value;
        }

        @MongoId
        public ObjectId getId() {
            return id;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public StringCreatorAndObjectId withObjectId(ObjectId id) {
            this.id = id;
            return this;
        }
    }

    @Test
    public void canSaveAndMapAPojoWithObjectIdCreator() throws Exception {

        ObjectId value = new ObjectId();

        collection.save(new ObjectIdCreator(value));

        ObjectIdCreator noId = collection.findOne().as(ObjectIdCreator.class);

        assertThat(noId.value).isEqualTo(value);
    }

    private static class ObjectIdCreator {
        final ObjectId value;

        @JsonCreator
        public ObjectIdCreator(@JsonProperty("value") ObjectId value) {
            this.value = value;
        }
    }

    @Test
    public void canSaveAndMapAPojoWithDateCreator() throws Exception {

        Date value = new Date();

        collection.save(new DateCreator(value));

        DateCreator noId = collection.findOne().as(DateCreator.class);

        assertThat(noId.value).isEqualTo(value);
    }

    private static class DateCreator {
        final Date value;

        @JsonCreator
        public DateCreator(@JsonProperty("value") Date value) {
            this.value = value;
        }
    }

    @Test
    public void canSaveWithDateFieldAndMapWithout() {
        DateField dateField = new DateField(new ObjectId());
        dateField.date = new BSONTimestamp();

        collection.save(dateField);

        IdOnly mapped = collection.findOne().as(IdOnly.class);

        assertThat(mapped._id).isEqualTo(dateField._id);
    }

    private static class IdOnly {
        public final ObjectId _id;

        @JsonCreator
        public IdOnly(@MongoId @JsonProperty ObjectId _id) {
            this._id = _id;
        }
    }

    private static class DateField extends IdOnly {
        @JsonProperty
        public BSONTimestamp date;

        @JsonCreator
        public DateField(@JsonProperty ObjectId _id) {
            super(_id);
        }
    }


}
