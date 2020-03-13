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

package org.jongo.marshall;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.*;
import org.jongo.MongoCollection;
import org.jongo.model.Friend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class DocumentMarshallingTest extends JongoTestBase {

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
        type.anEnum = Parameter.TEST;

        collection.save(type);

        assertHasBeenPersistedAs("{'number' : 100}");
        assertHasBeenPersistedAs("{'string' : 'value'}");
        assertHasBeenPersistedAs("{'bool' : false}");
        assertHasBeenPersistedAs("{'anEnum' : 'TEST'}");
        JavaNativeType result = collection.findOne("{}").as(JavaNativeType.class);
        assertThat(result.bool).isFalse();
        assertThat(result.string).isEqualTo("value");
        assertThat(result.number).isEqualTo(100L);
        assertThat(result.anEnum).isEqualTo(Parameter.TEST);
    }

    @Test
    public void canHandleMinAndMaxKey() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.minKey = new MinKey();
        type.maxKey = new MaxKey();

        collection.save(type);

        assertHasBeenPersistedAs("{'minKey' : { '$minKey' : 1}}");
        assertHasBeenPersistedAs("{'maxKey' : { '$maxKey' : 1}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.minKey).isNotNull();
        assertThat(result.maxKey).isNotNull();
    }

    @Test
    public void canHandleDecimal128() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.decimal128 = Decimal128.parse("10");

        collection.save(type);

        assertHasBeenPersistedAs("{'decimal128' : { '$type' : 'decimal'}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.decimal128).isNotNull();
    }

    @Test
    public void canHandleObjectId() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.key = new ObjectId("4fe83969e4b042bbbca47c48");

        collection.save(type);

        assertHasBeenPersistedAs("{'key' : { '$oid' : '4fe83969e4b042bbbca47c48'}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.key).isEqualTo(new ObjectId("4fe83969e4b042bbbca47c48"));
    }

    @Test
    public void canHandlePattern() throws Exception {

        Pattern chars = Pattern.compile("[a-z]");
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.pattern = chars;

        collection.save(type);

        assertHasBeenPersistedAs("{'pattern' : { '$regex' : '[a-z]'}}");//options is not longer generated since 2.8.0
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.pattern.toString()).isEqualTo(chars.toString());
    }

    @Test
    public void canHandleBSONTimestamp() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.timestamp = new BSONTimestamp(1, 2);

        collection.save(type);

        assertHasBeenPersistedAs("{'timestamp' : { '$timestamp' : { 't' : 1 , 'i' : 2}}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.timestamp).isEqualTo(new BSONTimestamp(1, 2));
    }

    @Test
    public void canHandleISODate() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.date = new Date(123);

        collection.save(type);

        assertHasBeenPersistedAs("{'date' : { '$date' : 123}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.date).isEqualTo(new Date(123));
    }

    @Test
    public void canHandleNonIsoDate() throws IOException {

        collection.insert("{date:#}", 1340714101235L);

        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);

        assertThat(result.date).isEqualTo(new Date(1340714101235L));
    }

    @Test
    public void canHandleUUID() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.uuid = UUID.fromString("cf0eddfa-2670-4929-a581-eb263d839cab");

        collection.save(type);

        assertHasBeenPersistedAs("{'uuid' : { '$uuid' : 'cf0eddfa-2670-4929-a581-eb263d839cab'}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.uuid).isEqualTo(type.uuid);
    }

    @Test
    public void canHandleDBObject() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.dbo = new BasicDBObject("key", "value");

        collection.save(type);

        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.dbo).isEqualTo(type.dbo);
    }

    @Test
    public void canHandleMapWithPrimitiveType() throws Exception {

        Map<String, Date> strings = new HashMap<String, Date>();
        strings.put("key", new Date(456));
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.mapWithDates = strings;

        collection.save(type);

        assertHasBeenPersistedAs("{mapWithDates : { key : { $date : 456}}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.mapWithDates).contains(entry("key", new Date(456)));
    }

    @Test
    public void canHandleMapWithComplexType() throws Exception {

        Map<String, Friend> friends = new HashMap<String, Friend>();
        Friend robert = new Friend("robert");
        friends.put("key", robert);
        BSONPrimitiveType type = new BSONPrimitiveType();
        type.friends = friends;

        collection.save(type);

        assertHasBeenPersistedAs("{'friends' : { 'key' : { 'name' : 'robert'}}}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.friends).contains(entry("key", robert));
    }

    @Test
    public void canHandleArray() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.array = new int[]{1, 2, 3};

        collection.save(type);

        assertHasBeenPersistedAs("{'array' : [ 1 , 2 , 3]}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.array).contains(1, 2, 3);
    }

    @Test
    public void canHandleByteArray() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        type.bytes = "this is a byte array".getBytes();

        collection.save(type);

        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.bytes).isEqualTo(type.bytes);
    }

    @Test
    public void canHandleIterableWithPrimitiveType() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        List<Date> dates = new ArrayList<Date>();
        dates.add(new Date(123));
        type.dateList = dates;

        collection.save(type);

        assertHasBeenPersistedAs("{'dateList' : [ { '$date' : 123}]}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.dateList).contains(new Date(123));
    }

    @Test
    public void canHandleIterableWithComplexType() throws Exception {

        BSONPrimitiveType type = new BSONPrimitiveType();
        List<Friend> friends = new ArrayList<Friend>();
        Friend robert = new Friend("robert");
        friends.add(robert);
        type.complexList = friends;

        collection.save(type);

        assertHasBeenPersistedAs("{'complexList' : [ { 'name' : 'robert'}]}");
        BSONPrimitiveType result = collection.findOne("{}").as(BSONPrimitiveType.class);
        assertThat(result.complexList).contains(robert);
    }

    private void assertHasBeenPersistedAs(String expectedPersistedJSON) {
        assertThat(collection.count(expectedPersistedJSON)).isEqualTo(1);
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
        byte[] bytes;
        Decimal128 decimal128;
    }

    private static class JavaNativeType {
        long number;
        String string;
        boolean bool;
        Parameter anEnum;
    }

    private static enum Parameter {
        TEST
    }

}
