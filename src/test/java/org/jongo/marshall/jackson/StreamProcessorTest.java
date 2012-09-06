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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.DBObject;
import org.jongo.marshall.MarshallingException;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.model.Views;
import org.jongo.util.ErrorObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.BSON.bsonify;

public class StreamProcessorTest {

    private StreamProcessor processor;

    @Before
    public void setUp() throws Exception {
        this.processor = new StreamProcessor();
    }

    @Test
    public void canConvertEntityToJson() {

        DBObject dbo = processor.marshall(new Fox("fantastic", "roux"));

        assertThat(dbo.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(dbo.get("name")).isEqualTo("fantastic");
        assertThat(dbo.get("color")).isEqualTo("roux");
    }

    @Test(expected = MarshallingException.class)
    public void shouldFailWhenUnableToMarshall() throws Exception {
        processor.marshall(new ErrorObject());
    }

    @Test
    public void canConvertJsonToEntity() throws IOException {
        DBObject document = bsonify("{'address': '22 rue des murlins'}");

        Friend friend = processor.unmarshall(document, Friend.class);

        assertThat(friend.getAddress()).isEqualTo("22 rue des murlins");
    }

    @Test
    public void canConvertNestedJsonToEntities() throws IOException {
        DBObject document = bsonify("{'address': '22 rue des murlins', 'coordinate': {'lat': 48}}");

        Friend friend = processor.unmarshall(document, Friend.class);

        assertThat(friend.getCoordinate().lat).isEqualTo(48);
    }

    @Test
    public void hasAFallbackToEnsureBackwardCompatibility() throws IOException {

        DBObject document = bsonify("{'oldAddress': '22-rue-des-murlins'}");

        BackwardFriend backwardFriend = processor.unmarshall(document, BackwardFriend.class);

        assertThat(backwardFriend.getAddress()).isEqualTo("22-rue-des-murlins");
    }

    @Test
    public void canHandleNonIsoDate() throws IOException {

        Date oldDate = new Date(1340714101235L);
        DBObject document = bsonify("{'oldDate': " + 1340714101235L + " }");

        BackwardFriend backwardFriend = processor.unmarshall(document, BackwardFriend.class);

        assertThat(backwardFriend.oldDate).isEqualTo(oldDate);
    }

    @Test
    public void shouldFailWhenUnableToUnmarshall() throws Exception {

        try {
            processor.unmarshall(bsonify("{'error':'notADate'}"), ErrorObject.class);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
        }
    }

    @Test
    public void respectsJsonPublicViewOnMarshall() throws Exception {

        StreamProcessor custom = createProcessorWithView(Views.Public.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        DBObject result = custom.marshall(vixen);

        assertThat(result.get("gender")).isNull();
        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
    }

    @Test
    public void respectsJsonPrivateViewOnMarshall() throws Exception {

        StreamProcessor custom = createProcessorWithView(Views.Private.class);
        Fox vixen = new Fox("fantastic", "roux");
        vixen.setGender("female");

        DBObject result = custom.marshall(vixen);

        assertThat(result.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(result.get("name")).isEqualTo("fantastic");
        assertThat(result.get("color")).isEqualTo("roux");
        assertThat(result.get("gender")).isEqualTo("female");
    }

    @Test
    public void respectsJsonPublicViewOnUnmarshall() throws Exception {

        DBObject json = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        StreamProcessor custom = createProcessorWithView(Views.Public.class);

        Fox fox = custom.unmarshall(json, Fox.class);

        assertThat(fox.getGender()).isNull();
    }

    @Test
    public void respectsJsonPrivateViewOnUnmarshall() throws Exception {

        DBObject json = bsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");
        StreamProcessor custom = createProcessorWithView(Views.Private.class);

        Fox fox = custom.unmarshall(json, Fox.class);

        assertThat(fox.getGender()).isEqualTo("female");
    }


    private StreamProcessor createProcessorWithView(Class<?> viewClass) {
        ObjectMapper mapper = new ObjectMapperFactory().createBsonMapper();
        ObjectReader reader = mapper.reader().withView(viewClass);
        ObjectWriter writer = mapper.writer().withView(viewClass);
        return new StreamProcessor(reader, writer);
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
