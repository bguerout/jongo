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

package org.jongo.marshall.jackson.oid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class AnnotationsTest {

    private ObjectMapper externalMapper;

    @Before
    public void setUp() throws Exception {
        externalMapper = new ObjectMapper();
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void shouldIgnoreIdAnnotation() throws IOException {
        externalMapper.readValue("{\"_id\":\"53a499be60b2a2248d956875\"}", WithMongoId.class);
    }

    @Test
    public void shouldHandleIdAnnotation() throws IOException {

        Mapping build = new Mapping.Builder().build();
        ObjectReader reader = build.getReader(WithMongoId.class);
        BsonDocument document = Bson.createDocument(new BasicDBObject("_id", "53a499be60b2a2248d956875"));

        WithMongoId friend = reader.readValue(document.toByteArray());

        assertThat(friend.id, equalTo("53a499be60b2a2248d956875"));
    }


    @Test(expected = JsonMappingException.class)
    public void shouldIgnoreObjectIdAnnotation() throws IOException {
        externalMapper.readValue("{\"id\":{\"$oid\":\"53a499be60b2a2248d956875\"}}", WithMongoObjectId.class);
    }

    @Test
    public void shouldHandleObjectIdAnnotation() throws IOException {

        Mapping build = new Mapping.Builder().build();
        ObjectReader reader = build.getReader(WithMongoObjectId.class);
        DBObject oid = new BasicDBObject("$oid", "53a499be60b2a2248d956875");
        BasicDBObject dbObject = new BasicDBObject("id", oid);
        BsonDocument document = Bson.createDocument(dbObject);

        WithMongoObjectId testObj = reader.readValue(document.toByteArray());

        assertThat(testObj.id, equalTo("53a499be60b2a2248d956875"));
    }

    private static class WithMongoObjectId {
        @MongoObjectId
        @JsonProperty
        String id;
    }

    private static class WithMongoId {
        @MongoId
        private String id;
    }
}
