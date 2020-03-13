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

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.JacksonIdFieldSelector;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.model.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectiveObjectIdUpdaterTest {

    private ReflectiveObjectIdUpdater updater;

    @Before
    public void setUp() throws Exception {
        updater = new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector());
    }

    @Test
    public void mustBeGeneratedWhenPojoHasAnEmptyObjectId() throws Exception {

        Friend friend = new Friend();

        boolean hasOid = updater.mustGenerateObjectId(friend);

        assertThat(hasOid).isTrue();
    }

    @Test
    public void mustNotBeGeneratedWhenPojoHasAnObjectId() throws Exception {

        Friend friend = new Friend(ObjectId.get(), "John");

        boolean hasOid = updater.mustGenerateObjectId(friend);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void mustNotBeGeneratedWhenPojoDoesNotMapObjectId() throws Exception {

        Coordinate coordinate = new Coordinate(1, 1);

        assertThat(updater.mustGenerateObjectId(coordinate)).isFalse();
    }

    @Test
    public void mustBeGeneratedWhenPojoHasAnEmptyObjectId_and_AParentWithAnObjectId() throws Exception {

        ChildWithId child = new ChildWithId();
        child.id_parent = ObjectId.get();

        boolean hasOid = updater.mustGenerateObjectId(child);

        assertThat(hasOid).isTrue();
    }

    @Test
    public void mustNotBeGeneratedWhenPojoHasAnObjectId_and_AParentWithAnObjectId() throws Exception {

        ChildWithId child = new ChildWithId();
        child.id = ObjectId.get();
        child.id_parent = ObjectId.get();

        boolean hasOid = updater.mustGenerateObjectId(child);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void mustNotBeGeneratedWhenPojoHasAnObjectId_and_AParentWithAnEmptyObjectId() throws Exception {

        ChildWithId child = new ChildWithId();
        child.id = ObjectId.get();

        boolean hasOid = updater.mustGenerateObjectId(child);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void mustNotBeGeneratedWhenPojoHasntObjectIdMapping_and_AParentWithAnObjectId() throws Exception {

        Child child = new Child();
        child.id_parent = ObjectId.get();

        boolean hasOid = updater.mustGenerateObjectId(child);

        assertThat(hasOid).isFalse();
    }

    @Test
    public void mustNotBeGeneratedWhenPojoHasCustomObjectId() throws Exception {
        assertThat(updater.mustGenerateObjectId(new ExternalFriend(null, "John"))).isFalse();
        assertThat(updater.mustGenerateObjectId(new ExternalFriend("id", "John"))).isFalse();
    }

    @Test
    public void canFindObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        Friend friend = new Friend(oid, "john");

        Object foundId = updater.getId(friend);

        assertThat(oid).isEqualTo(foundId);
    }

    @Test
    public void canFindNullObjectId() throws Exception {

        Object foundId = updater.getId(new Friend("john"));

        assertThat(foundId).isNull();
    }

    @Test
    public void canFindNullId() throws Exception {

        Object foundId = updater.getId(new Coordinate(1, 1));

        assertThat(foundId).isNull();
    }

    @Test
    public void canSetObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        Friend friend = new Friend();

        updater.setObjectId(friend, oid);

        assertThat(friend.getId()).isEqualTo(oid);
    }

    @Test
    public void canSetObjectIdValueAsString() throws Exception {

        ObjectId oid = new ObjectId();
        ExposableFriend target = new ExposableFriend(null, "John");

        updater.setObjectId(target, oid);

        assertThat(target.getId()).isEqualTo(oid.toString());
    }

    @Test
    public void shouldNotChangeOtherObjectIdField() throws IOException {

        ObjectId relationId = new ObjectId();
        LinkedFriend friend = new LinkedFriend(relationId);

        updater.setObjectId(friend, ObjectId.get());

        assertThat(friend.getRelationId()).isNotEqualTo(friend.getId());
        assertThat(friend.getRelationId()).isEqualTo(relationId);
    }

    @Test
    public void shouldIgnoreWhenObjectIdDoesntExist() throws Exception {
        updater.setObjectId(new Coordinate(1, 1), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canSetNewObjectId() throws Exception {
        Friend john = new Friend(ObjectId.get(), "John");
        ObjectId newOid = ObjectId.get();

        updater.setObjectId(john, newOid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetCustomId() throws Exception {
        updater.setObjectId(new ExternalFriend("122", "John"), ObjectId.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSetPreexistingCustomId() throws Exception {
        final ExternalFriend custom = new ExternalFriend("122", "value");
        updater.setObjectId(custom, ObjectId.get());
    }

    private static class Parent {
        @Id
        ObjectId id_parent;
    }

    private static class Child extends Parent {

    }

    private static class ChildWithId extends Parent {
        @Id
        ObjectId id;
    }
}
