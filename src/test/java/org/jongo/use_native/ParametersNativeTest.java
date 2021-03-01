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

package org.jongo.use_native;

import com.mongodb.client.MongoCollection;
import org.jongo.model.Friend;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ParametersNativeTest extends NativeTestBase {

    private MongoCollection<Friend> collection;

    @Before
    public void setUp() throws Exception {
        collection = jongo.getCollection("friends", Friend.class);
    }

    @After
    public void tearDown() throws Exception {
        collection.drop();
    }

    @Test
    //https://groups.google.com/forum/?fromgroups#!topic/jongo-user/p9CEKnkKX9Q
    public void canUpdateIntoAnArray() throws Exception {

        collection.insertMany(asList(new Friend("Peter"), new Friend("Robert")));

        collection.updateMany(q("{ 'name' : 'Peter' }"), q("{ $set : # }", new Friend("John")));

        Friend friend = collection.find(q("{ 'name' : 'John' }")).first();

        assertThat(friend).isNotNull();
        assertThat(friend.getName()).isEqualTo("John");
    }


}
