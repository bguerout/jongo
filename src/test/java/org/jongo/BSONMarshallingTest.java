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
import static org.fest.assertions.MapAssert.entry;
import static org.jongo.util.JSONResultMapper.jsonify;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.jongo.model.People;
import org.jongo.util.JSONResultMapper;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class BSONMarshallingTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("primitives");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("primitives");
    }

    @Test
    public void canHandleJavaTypes() throws Exception {

        JavaNativeType type = new JavaNativeType();
        type.number = 100L;
        type.string = "value";
        type.bool = false;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'number' : 100 , 'string' : 'value' , 'bool' : false"));
        JavaNativeType result = collection.findOne("{}").as(JavaNativeType.class);
        assertThat(result.bool).isFalse();
        assertThat(result.string).isEqualTo("value");
        assertThat(result.number).isEqualTo(100L);
    }

    @Test
    public void canHandleMinAndMaxKey() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.minKey = new MinKey();
        type.maxKey = new MaxKey();

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'minKey' : { '$minKey' : 1} , 'maxKey' : { '$maxKey' : 1}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.minKey).isNotNull();
        assertThat(result.maxKey).isNotNull();
    }

    @Test
    public void canHandleObjectId() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.key = new ObjectId("4fe83969e4b042bbbca47c48");

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'key' : { '$oid' : '4fe83969e4b042bbbca47c48'}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.key).isEqualTo(new ObjectId("4fe83969e4b042bbbca47c48"));
    }

    @Test
    public void canHandlePattern() throws Exception {

        Pattern chars = Pattern.compile("[a-z]");
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.pattern = chars;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'pattern' : { '$regex' : '[a-z]' , '$options' : ''}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.pattern.toString()).isEqualTo(chars.toString());
    }

    @Test
    public void canHandleBSONTimestamp() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.timestamp = new BSONTimestamp(1, 2);

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'timestamp' : { '$ts' : 1 , '$inc' : 2}}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.timestamp).isEqualTo(new BSONTimestamp(1, 2));
    }

    @Test
    public void canHandleISODate() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.date = new Date(0);

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'date' : { '$date' : '1970-01-01T00:00:00.000Z'}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.date).isEqualTo(new Date(0));
    }

    @Test
    public void canHandleUUID() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.uuid = UUID.fromString("cf0eddfa-2670-4929-a581-eb263d839cab");

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'uuid' : { '$uuid' : 'cf0eddfa-2670-4929-a581-eb263d839cab'}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.uuid).isEqualTo(type.uuid);
    }

    @Test
    public void canHandleCode() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.code = new Code("code");

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'code' : { '$code' : 'code'}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.code).isEqualTo(type.code);
    }

    @Test
    public void canHandleDBObject() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.dbo = new BasicDBObject("key", "value");

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'dbo' : { 'key' : 'value'}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.dbo).isEqualTo(type.dbo);
    }

    @Test
    public void canHandleMap() throws Exception {

        Map<String, String> strings = new HashMap<String, String>();
        strings.put("key", "value");
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.map = strings;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'map' : { 'key' : 'value'}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.map).includes(entry("key", "value"));
    }

    @Test
    public void canHandleMapWithComplexType() throws Exception {

        Map<String, People> peoples = new HashMap<String, People>();
        People robert = new People("robert");
        peoples.put("key", robert);
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.peoples = peoples;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'peoples' : { 'key' : { 'name' : 'robert'}}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.peoples).includes(entry("key", robert));
    }

    @Test
    public void canHandleArray() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.array = new int[]{1, 2, 3};

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'array' : [ 1 , 2 , 3]"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.array).contains(1, 2, 3);
    }

    @Test
    public void canHandleIterableWithComplexType() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        List<People> peoples = new ArrayList<People>();
        People robert = new People("robert");
        peoples.add(robert);
        type.peopleList = peoples;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'peopleList' : [ { 'name' : 'robert'}]"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.peopleList).contains(robert);
    }

    private void assertHasBeenPersistedAs(String expectedPersistedJSON) {
        String result = collection.findOne("{}").map(new JSONResultMapper());
        assertThat(result).contains(expectedPersistedJSON);
    }

    private static class BSONPrimitiveType {
        MinKey minKey;
        MaxKey maxKey;
        ObjectId key;
        Pattern pattern;
        BSONTimestamp timestamp;
        Date date;
        UUID uuid;
        Code code;
        DBObject dbo;
        Map<String, String> map;
        Map<String, People> peoples;
        int[] array;
        List<People> peopleList;
    }

    private static class JavaNativeType {
        long number;
        String string;
        boolean bool;
    }

}
