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

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;

import org.jongo.marshall.MarshallingException;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.model.Views;
import org.jongo.util.UnmarshallableObject;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JacksonProcessorTest {

    private JacksonProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new JacksonProcessor();
    }

    @Test
    public void canConvertEntityToJson() {
        String json = processor.marshall(new Fox("fantastic", "roux"));
        assertThat(json).isEqualTo(jsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux'}"));

        Friend friend = processor.unmarshall(json, Friend.class);
        assertThat(friend.getName()).isEqualTo("fantastic");
    }

    @Test
    public void canConvertJsonToEntity() throws IOException {
        String json = jsonify("{'address': '22 rue des murlins'}");

        Friend friend = processor.unmarshall(json, Friend.class);

        assertThat(friend.getAddress()).isEqualTo("22 rue des murlins");
    }

    @Test
    public void canConvertNestedJsonToEntities() throws IOException {
        String json = jsonify("{'address': '22 rue des murlins', 'coordinate': {'lat': 48}}");

        Friend friend = processor.unmarshall(json, Friend.class);

        assertThat(friend.getCoordinate().lat).isEqualTo(48);
    }

    @Test
    public void hasAFallbackToEnsureBackwardCompatibility() throws IOException {

        String json = jsonify("{'address': '22 rue des murlins', 'oldAddress': '22-rue-des-murlins'}");

        BackwardFriend backwardFriend = processor.unmarshall(json, BackwardFriend.class);

        assertThat(backwardFriend.getAddress()).isEqualTo("22-rue-des-murlins");
    }

    @Test
    public void canHandleNonIsoDate() throws IOException {

        Date oldDate = new Date(1340714101235L);
        String json = jsonify("{'oldDate': " + 1340714101235L + " }");

        BackwardFriend backwardFriend = processor.unmarshall(json, BackwardFriend.class);

        assertThat(backwardFriend.oldDate).isEqualTo(oldDate);
    }

    @Test
    public void shouldFailWhenUnableToUnmarshall() throws Exception {

        try {
            processor.unmarshall("{error:'notADate'}", UnmarshallableObject.class);
            fail();
        } catch (MarshallingException e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
            assertThat(e.getMessage()).contains("{error:'notADate'}");
        }
    }

    @Test
    public void shouldFailWhenUnableToMarshall() throws Exception {

        try {
            processor.marshall(new UnmarshallableObject());
            fail();
        } catch (MarshallingException e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
        }
    }
    
    @Test
    public void respectsJsonViewOnMarshall() throws Exception {
    	processor = createProcessorWithView(Views.Public.class);
    	Fox vixen = new Fox("fantastic", "roux");
    	vixen.setGender("female");
    	String json = processor.marshall(vixen);
        assertThat(json).isEqualTo(jsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux'}"));
        
    	processor = createProcessorWithView(Views.Private.class);
    	json = processor.marshall(vixen);
    	assertThat(json).isEqualTo(jsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}"));
    }

    @Test
    public void respectsJsonViewOnUnmarshall() throws Exception {
    	String json = jsonify("{'_class':'org.jongo.model.Fox','name':'fantastic','color':'roux','gender':'female'}");

    	processor = createProcessorWithView(Views.Public.class);
    	Fox fox = processor.unmarshall(json, Fox.class);
    	assertThat(fox.getGender()).isNull();

    	processor = createProcessorWithView(Views.Private.class);
    	fox = processor.unmarshall(json, Fox.class);
    	assertThat(fox.getGender()).isEqualTo("female");
    }

    private JacksonProcessor createProcessorWithView(Class<?> viewClass) {
    	ObjectMapper mapper = JacksonProcessor.createPreConfiguredMapper();
    	ObjectReader reader = mapper.reader().withView(viewClass);
    	ObjectWriter writer = mapper.writer().withView(viewClass);
    	return new JacksonProcessor(reader, writer);
    }
    
    private String jsonify(String json) {
        return json.replace("'", "\"");
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
