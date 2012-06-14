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

package org.jongo.spike.dbref;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.JongoTest.collection;

import java.net.UnknownHostException;

import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.JongoTest;
import org.jongo.MongoCollection;
import org.jongo.ResultMapper;
import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.spike.dbref.jackson.Reference;
import org.jongo.spike.dbref.jackson.ReferenceDeserializer;
import org.jongo.spike.dbref.jackson.ReferenceLink;
import org.jongo.spike.dbref.jackson.ReferenceSerializer;
import org.jongo.util.DBObjectResultMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;

public class DBRefSpikeTest {

    @Rule
    public JongoTest jongo = JongoTest.collection("buddies");

    private ObjectId johnId;

    @Before
    public void setUp() throws Exception {
        Buddy john = new Buddy();
        john.name = "John";
        johnId = new ObjectId(collection.save(john));
    }

    @Test
    public void canParseDBRef() throws Exception {
        DBRef ref = (DBRef) JSON.parse("{ '$ref' : 'users', $id : 2222 }");
        assertThat(ref.getRef()).isEqualTo("users");
    }

    @Test
    public void referenceShouldBeInserted() throws Exception {
        collection.insert("{name : 'Abby', friend: { $ref : 'buddies', $id : # }}", johnId);

        DBObject abby = collection.findOne("{name : 'Abby'}").map(new DBObjectResultMapper());

        Object ref = abby.get("friend");
        assertThat(ref).isInstanceOf(DBRef.class);
    }

    @Test
    public void referenceShouldBeFetcheableWithMapper() throws Exception {
        collection.insert("{name : 'Abby', friend: { $ref : 'buddies', $id : # }}", johnId);
        DBObject abby = collection.findOne("{name : 'Abby'}").map(new DBObjectResultMapper());

        DBRef ref = (DBRef) abby.get("friend");
        DBObject johnAsDbObject = ref.fetch();

        assertThat(johnAsDbObject.get("name")).isEqualTo("John");
    }

    @Test
    public void referenceShouldBeUnmarshalledWithJackson() throws Exception {
        MongoCollection buddies = getCollectionWithCustomMapper();
        buddies.insert("{name : 'Abby', friend: { $ref : 'buddies', $id : # }}", johnId);

        Buddy abby = buddies.findOne("{name : 'Abby'}").as(Buddy.class);

        assertThat(abby.friend).isNotNull();
        assertThat(abby.friend.name).isEqualTo("John");
    }

    @Test
    public void referenceShouldBeMarshalledWithJackson() throws Exception {

        Buddy peter = new Buddy("Peter", null);
        Buddy buddy = new Buddy("Abby", peter);
        MongoCollection buddies = getCollectionWithCustomMapper();
        final String peterId = buddies.save(peter);
        peter.id = new ObjectId(peterId);

        buddies.save(buddy);

        buddies.findOne("{name : 'Abby'}").map(new ResultMapper<DBObject>() {
            public DBObject map(DBObject result) {
                assertThat(result.get("friend")).isInstanceOf(DBRef.class);
                assertThat(((DBRef) result.get("friend")).getId()).isEqualTo(peterId);
                return result;
            }
        });
    }

    private MongoCollection getCollectionWithCustomMapper() throws UnknownHostException {
        DB db = jongo.getDatabase();
        ObjectMapper mapper = createMapper(db);
        JacksonProcessor processor = new JacksonProcessor(mapper);
        Jongo jongo = new Jongo(db, processor, processor);
        return jongo.getCollection("buddies");
    }

    private ObjectMapper createMapper(DB database) {
        ObjectMapper mapper = JacksonProcessor.createMinimalMapper();
        SimpleModule dbRefModule = createDBRefModule(database, mapper);
        mapper.registerModule(dbRefModule);
        return mapper;
    }

    private SimpleModule createDBRefModule(DB database, ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("dbRefModule", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(Reference.class, new ReferenceDeserializer(mapper, database));

        ReferenceSerializer serializer = new ReferenceSerializer();
        serializer.registerReferenceLink(Buddy.class, new ReferenceLink<Buddy>() {
            public String getReferenceCollectionName(Buddy buddy) {
                return "buddies";
            }

            public String getId(Buddy buddy) {
                return buddy.id.toString();
            }
        });
        module.addSerializer(Reference.class, serializer);
        return module;
    }

}
