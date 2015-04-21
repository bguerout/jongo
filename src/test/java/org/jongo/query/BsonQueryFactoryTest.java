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

package org.jongo.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class BsonQueryFactoryTest {

    private QueryFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new BsonQueryFactory(new JacksonEngine(Mapping.defaultMapping()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidParameter() throws Exception {

        factory.createQuery("{id:#}", new ErrorObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNotEnoughParameters() throws Exception {

        factory.createQuery("{id:#,id2:#}", "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTooManyParameters() throws Exception {

        factory.createQuery("{id:#}", 123, 456);
    }

    @Test
    public void shouldAllowToCreateNullQuery() throws Exception {

        Query query = factory.createQuery(null);

        assertThat(query.toDBObject()).isEqualTo(null);
    }

    @Test
    public void shouldBindOneParameter() throws Exception {

        Query query = factory.createQuery("{id:#}", 123);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", 123));
    }

    @Test
    public void shouldBindManyParameters() throws Exception {

        Query query = factory.createQuery("{id:#, test:#}", 123, 456);

        assertThat(query.toDBObject()).isEqualTo(QueryBuilder.start("id").is(123).and("test").is(456).get());
    }

    @Test
    public void shouldBindNullParameter() throws Exception {

        Query query = factory.createQuery("{id:#}", null);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", null));
    }

    @Test
    public void shouldBindParameterWithCustomToken() throws Exception {

        QueryFactory factoryWithToken = new BsonQueryFactory(new JacksonEngine(Mapping.defaultMapping()), "@");

        Query query = factoryWithToken.createQuery("{id:@}", 123);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", 123));
    }

    @Test
    public void shouldRemoveNullField() throws Exception {

        QueryFactory factoryWithToken = new BsonQueryFactory(new JacksonEngine(Mapping.defaultMapping()), true);

        Query query = factoryWithToken.createQuery("{name:#}", null);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject());
    }

    @Test
    public void shouldRemoveNullFields() throws Exception {

        QueryFactory factoryWithToken = new BsonQueryFactory(new JacksonEngine(Mapping.defaultMapping()), true);

        Query query = factoryWithToken.createQuery("{name:#, firstname:#, lastname:#}", null, "John", null);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("firstname", "John"));
    }

    @Test
    public void shouldRemoveNullSubFields() throws Exception {

        QueryFactory factoryWithToken = new BsonQueryFactory(new JacksonEngine(Mapping.defaultMapping()), true);

        Query query = factoryWithToken.createQuery("{name: #, timestamp: { $lt: # }}", "John", null);

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("name", "John"));
    }

    @Test
    public void shouldBindHashSign() throws Exception {

        Query query = factory.createQuery("{id:#}", "string with # sign");

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", "string with # sign"));
    }

    @Test
    public void shouldBindQuotationMark() throws Exception {

        Query query = factory.createQuery("{id:#}", "string with \" quotation mark");

        assertThat(query.toDBObject()).isEqualTo(new BasicDBObject("id", "string with \" quotation mark"));
    }

    @Test
    public void shouldBindNestedQuery() throws Exception {

        DBObject dbo = factory.createQuery("{ a: #, b: { c: #, d: #}, e: #}", 1, Long.valueOf(2), 3, "hello").toDBObject();

        assertThat(dbo.get("a")).isEqualTo(Integer.valueOf(1));
        assertThat(dbo.get("e")).isEqualTo("hello");

        DBObject dbo2 = (DBObject) dbo.get("b");
        assertThat(dbo2.get("c")).isEqualTo(Long.valueOf(2));
        assertThat(dbo2.get("d")).isEqualTo(Integer.valueOf(3));
    }

    public static class AnObject {
        public int aa;
        public long bb;
    }

    @Test
    public void shouldBindNestedParameter() throws Exception {
        AnObject ao = new AnObject();
        ao.aa = 10;
        ao.bb = 11;

        DBObject dbo = factory.createQuery("{ a: #, b: { c: #, d: #}, e: #}", 1, Long.valueOf(2), ao, "hello").toDBObject();

        assertThat(dbo.get("a")).isEqualTo(Integer.valueOf(1));
        assertThat(dbo.get("e")).isEqualTo("hello");

        DBObject dbo2 = (DBObject) dbo.get("b");
        assertThat(dbo2.get("c")).isEqualTo(Long.valueOf(2));

        DBObject dbo3 = (DBObject) dbo2.get("d");
        assertThat(dbo3.get("aa")).isEqualTo(Integer.valueOf(10));
        assertThat(dbo3.get("bb")).isEqualTo(Long.valueOf(11));
    }

    @Test
    public void canSaveAndUpdateBytes() throws Exception {

        DBObject dbo = factory.createQuery("{bytes:#}", "test".getBytes(StandardCharsets.UTF_8)).toDBObject();

        assertThat(new String((byte[]) dbo.get("bytes"))).isEqualTo("test");
    }

    @Test
    public void canHandleObjectSerializedAsAPrimitive() throws Exception {

        Mapping mapping = new Mapping.Builder().addSerializer(Friend.class, new PrimitiveJsonSerializer()).build();
        factory = new BsonQueryFactory(new JacksonEngine(mapping));

        DBObject query = factory.createQuery("{bytes:#}", new Friend("Robert")).toDBObject();

        assertThat(query.get("bytes")).isEqualTo("Robert");
    }

    @Test
    public void shouldBindKeyParameter() throws Exception {

        Query query = factory.createQuery("{#: 123}", "id");

        assertThat(query.toDBObject()).isEqualTo(QueryBuilder.start("id").is(123).get());
    }

    @Test
    public void shouldBindKeyParameterAndIgnoreSpace() throws Exception {

        Query query = factory.createQuery("{ #: 123}", "id");

        assertThat(query.toDBObject()).isEqualTo(QueryBuilder.start("id").is(123).get());
    }

    @Test
    public void shouldBindKeyParameterInSecondPosition() throws Exception {

        Query query = factory.createQuery("{a: 'a', #: 'b'}", "id");

        assertThat(query.toDBObject()).isEqualTo(QueryBuilder.start("a").is("a").and("id").is("b").get());
    }

    @Test
    public void shouldBindOneValueInAnArray() throws Exception {

        Query query = factory.createQuery("{a: [ # ]}", "test");

        DBObject expected = QueryBuilder.start("a").is(new String[]{"test"}).get();
        assertThat(query.toDBObject().toString()).isEqualTo(expected.toString());
    }

    @Test
    public void shouldBindManyValuesInAnArray() throws Exception {

        Query query = factory.createQuery("{a: [#, 'test2', #]}", "test1", "test3");

        DBObject expected = QueryBuilder.start("a").is(new String[]{"test1", "test2", "test3"}).get();
        assertThat(query.toDBObject().toString()).isEqualTo(expected.toString());
    }

    @Test
    public void shouldBindANestedKeyParameter() throws Exception {

        Query query = factory.createQuery("{ name.#: 'John'}", "first");

        assertThat(query.toDBObject().toString()).isEqualTo("{ \"name.first\" : \"John\"}");
    }

    @Test
    public void shouldBindASingleTokenAsParameter() throws Exception {

        Query query = factory.createQuery("#", new Friend("John"));

        assertThat(query.toDBObject()).isEqualTo(QueryBuilder.start("name").is("John").get());
    }

    private static class PrimitiveJsonSerializer extends JsonSerializer<Friend> {

        @Override
        public void serialize(Friend friend, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(friend.getName());
        }
    }
}
