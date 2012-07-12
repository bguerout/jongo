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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.jongo.marshall.MarshallingException;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.BSON.bsonify;
import static org.jongo.util.BSON.jsonify;

public class JacksonProcessorTest {

    private JacksonProcessor processor;

    @Before
    public void setUp() throws Exception {
        this.processor = new JacksonProcessor();
    }

    @Test
    public void canConvertEntityToJson() {
        String json = processor.marshall(new Fox("fantastic", "roux"));
        assertThat(json).isEqualTo(jsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux'}"));
    }

    @Test
    public void canConvertJsonToEntity() throws IOException {
        byte[] bson = bsonify("{'address': '22 rue des murlins'}");

        Friend friend = processor.unmarshall(bson, 0, Friend.class);

        assertThat(friend.getAddress()).isEqualTo("22 rue des murlins");
    }

    @Test
    public void canConvertNestedJsonToEntities() throws IOException {
        byte[] bson = bsonify("{'address': '22 rue des murlins', 'coordinate': {'lat': 48}}");

        Friend friend = processor.unmarshall(bson, 0, Friend.class);

        assertThat(friend.getCoordinate().lat).isEqualTo(48);
    }

    @Test
    public void hasAFallbackToEnsureBackwardCompatibility() throws IOException {

        byte[] bson = bsonify("{'oldAddress': '22-rue-des-murlins'}");

        BackwardFriend backwardFriend = processor.unmarshall(bson, 0, BackwardFriend.class);

        assertThat(backwardFriend.getAddress()).isEqualTo("22-rue-des-murlins");
    }

    @Test
    public void canHandleNonIsoDate() throws IOException {

        Date oldDate = new Date(1340714101235L);
        byte[] bson = bsonify("{'oldDate': " + 1340714101235L + " }");

        BackwardFriend backwardFriend = processor.unmarshall(bson, 0, BackwardFriend.class);

        assertThat(backwardFriend.oldDate).isEqualTo(oldDate);
    }

    @Test
    public void shouldFailWhenUnableToUnmarshall() throws Exception {

        try {
            processor.unmarshall(bsonify("{'error':'notADate'}"), 0, ErrorObject.class);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
        }
    }

    @Test
    public void shouldFailWhenUnableToMarshall() throws Exception {

        try {
            processor.marshall(new ErrorObject());
            fail();
        } catch (MarshallingException e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
        }
    }

    private static class BackwardFriend extends Friend {

        Date oldDate;

        @JsonAnySetter
        public void fallbackForBackwardCompatibility(String name, Object value) {
            if ("oldAddress".equals(name)) {
                setAddress((String) value);
            }
        }
    }
}
