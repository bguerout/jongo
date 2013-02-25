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
import org.jongo.model.Coordinate;
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
    public void shouldFindObjectId() throws Exception {

        Friend friend = new Friend();

        boolean hasOid = updater.canSetObjectId(friend);

        assertThat(hasOid).isTrue();
    }

    @Test
    public void shouldIgnoreNonNullObjectId() throws Exception {

        Friend friend = new Friend(ObjectId.get(), "John");

        boolean hasOid = updater.canSetObjectId(friend);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void shouldIgnoreWhenNoObjectId() throws Exception {

        Coordinate coordinate = new Coordinate(1, 1);

        assertThat(updater.canSetObjectId(coordinate)).isFalse();
    }

    @Test
    public void shoudFindObjectIdInParent() throws Exception {

        Child child = new Child();

        boolean hasOid = updater.canSetObjectId(child);

        assertThat(hasOid).isTrue();
    }

    @Test
    public void shouldIgnoreWhenNoDeclaredField() throws Exception {

        Object o = new Object();

        assertThat(updater.canSetObjectId(o)).isFalse();
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

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetWhenNoObjectIdExists() throws Exception {
        updater.setObjectId(new Coordinate(1, 1), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetPreexistingObjectId() throws Exception {
        updater.setObjectId(new Friend(ObjectId.get(), "John"), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetObject() throws Exception {
        updater.setObjectId(new Object(), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetCustomId() throws Exception {
        updater.setObjectId(new WithCustomId(), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetPreexistingCustomId() throws Exception {
        final WithCustomId custom = new WithCustomId();
        custom._id = 122;
        updater.setObjectId(custom, ObjectId.get());
    }

    private static class WithCustomId {
        private Integer _id;
    }

    private static class PojoWithStringId {
        protected String _id;
    }

    private static class Parent {
        ObjectId _id;
    }

    private static class Child extends Parent {

    }
}
