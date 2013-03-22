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

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.JacksonIdFieldSelector;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.model.Coordinate;
import org.jongo.model.ExternalFriend;
import org.jongo.model.Friend;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ReflectiveObjectIdUpdaterTest {

    private ReflectiveObjectIdUpdater updater;

    @Before
    public void setUp() throws Exception {
        updater = new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector());
    }

    @Test
    public void isNewWhenPojoHasObjectId() throws Exception {

        Friend friend = new Friend();

        boolean hasOid = updater.isNew(friend);

        assertThat(hasOid).isTrue();
    }

    @Test
    public void isNewWhenPojoHasnotObjectId() throws Exception {

        Coordinate coordinate = new Coordinate(1, 1);

        assertThat(updater.isNew(coordinate)).isTrue();
    }

    @Test
    public void isNotNewWhenPojoHasObjectIdWithValue() throws Exception {

        Friend friend = new Friend(ObjectId.get(), "John");

        boolean hasOid = updater.isNew(friend);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void isNewWhenObjectIdIsInParent() throws Exception {

        Child child = new Child();

        boolean hasOid = updater.isNew(child);

        assertThat(hasOid).isTrue();
    }

    @Test
    public void isNewWhenObjectIdIsInChildAndInParent() throws Exception {

        ChildWithId child = new ChildWithId();
        child.id_parent = ObjectId.get();

        boolean hasOid = updater.isNew(child);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void canSetObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        Friend friend = new Friend();

        updater.setObjectId(friend, oid);

        assertThat(friend.getId()).isEqualTo(oid);
    }

    @Test
    public void canSetStringIdWithObjectIdValue() throws Exception {

        ObjectId oid = new ObjectId();
        PojoWithStringId target = new PojoWithStringId();

        updater.setObjectId(target, oid);

        assertThat(target._id).isEqualTo(oid.toString());
    }

    @Test
    public void shouldIgnoreWhenObjectIdDoesntExist() throws Exception {
        updater.setObjectId(new Coordinate(1, 1), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetPreexistingObjectId() throws Exception {
        updater.setObjectId(new Friend(ObjectId.get(), "John"), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetCustomId() throws Exception {
        updater.setObjectId(new ExternalFriend(), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetPreexistingCustomId() throws Exception {
        final ExternalFriend custom = new ExternalFriend(122, "value");
        updater.setObjectId(custom, ObjectId.get());
    }

    private static class PojoWithStringId {
        protected String _id;
    }

    private static class Parent {
        @Id
        ObjectId id_parent;
    }

    private static class Child extends Parent {

    }

    private static class ChildWithId extends Parent {
        @Id
        Object id;
    }
}
