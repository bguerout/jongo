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

package org.jongo;


import com.mongodb.WriteConcern;
import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.model.ExternalFriend;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationsMisusedTest extends JongoTestBase {

    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        collection = createEmptyCollection("friends");
    }

    @After
    public void tearDown() throws Exception {
        dropCollection("friends");
    }

    @Test
    public void savingAPojoWithAnEmptyStringCustomId() throws Exception {

        ExternalFriend friend = ExternalFriend.createFriendWithoutId("Robert");

        collection.withWriteConcern(WriteConcern.MAJORITY).save(friend);

        ExternalFriend externalFriend = collection.findOne().as(ExternalFriend.class);

        /*
        * Works because Bson4jackson try to convert ObjectId into String
        * see com.fasterxml.jackson.databind.deser.std.StringDeserializer
        */
        assertThat(ObjectId.isValid(externalFriend.getId())).isTrue();
    }

    @Test
    public void savingAPojoWithAnEmptyCustomIntegerId() throws Exception {

        WithIntegerId custom = new WithIntegerId();

        collection.withWriteConcern(WriteConcern.MAJORITY).save(custom);

        try {
            collection.findOne().as(WithIntegerId.class);
            Assert.fail("Should not be able to unmarshall an ObjectId into an Integer");
        } catch (Exception e) {
        }
    }

    private static class WithIntegerId {
        @Id//see NewAnnotationsCompatibilitySuiteTest for more informations
        @MongoId
        private Integer id;
    }
}
