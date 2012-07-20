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

package org.jongo.marshall;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.*;
import org.jongo.MongoCollection;
import org.jongo.model.Friend;
import org.jongo.util.JSONResultMapper;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;
import static org.jongo.util.JSONResultMapper.jsonify;

public class DocumentMarshallingTest extends JongoTestCase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("marshalling");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("marshalling");
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
    public void testName() throws Exception {
        collection.getDBCollection().save(new BasicDBObject("key",new MinKey()));
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

        assertHasBeenPersistedAs(jsonify("'pattern' : { '$regex' : '[a-z]'"));//options is not longer generated since 2.8.0
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

        assertHasBeenPersistedAs(jsonify("'code' : { '_code' : 'code'}"));
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
    public void canHandleMapWithPrimitiveType() throws Exception {

        Map<String, Date> strings = new HashMap<String, Date>();
        strings.put("key", new Date(0));
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.mapWithDates = strings;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'mapWithDates' : { 'key' : { '$date' : '1970-01-01T00:00:00.000Z'}}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.mapWithDates).includes(entry("key", new Date(0)));
    }

    @Test
    public void canHandleMapWithComplexType() throws Exception {

        Map<String, Friend> friends = new HashMap<String, Friend>();
        Friend robert = new Friend("robert");
        friends.put("key", robert);
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.friends = friends;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'friends' : { 'key' : { 'name' : 'robert'}}"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.friends).includes(entry("key", robert));
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
    public void canHandleIterableWithPrimitiveType() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        List<Date> dates = new ArrayList<Date>();
        dates.add(new Date(0));
        type.dateList = dates;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'dateList' : [ { '$date' : '1970-01-01T00:00:00.000Z'}]"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.dateList).contains(new Date(0));
    }

    @Test
    public void canHandleIterableWithComplexType() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        List<Friend> friends = new ArrayList<Friend>();
        Friend robert = new Friend("robert");
        friends.add(robert);
        type.complexList = friends;

        collection.save(type);

        assertHasBeenPersistedAs(jsonify("'complexList' : [ { 'name' : 'robert'}]"));
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.complexList).contains(robert);
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
        Map<String, Date> mapWithDates;
        Map<String, Friend> friends;
        int[] array;
        List<Friend> complexList;
        List<Date> dateList;
    }

    private static class JavaNativeType {
        long number;
        String string;
        boolean bool;
    }

}
