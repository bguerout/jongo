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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.JacksonIdFieldSelector;
import org.jongo.marshall.jackson.id.Id;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.fest.assertions.Assertions.assertThat;

public class ReflectiveObjectIdUpdaterTest {

    private ReflectiveObjectIdUpdater updater;

    @Before
    public void setUp() throws Exception {
        updater = new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector());
    }

    @Test
    public void shouldFindObjectIdUsingIdAnnotation() throws Exception {

        Field field = updater.findFieldOrNull(WithIdAnnotation.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("key");
    }

    @Test
    public void shouldFindObjectIdUsingJsonPropertyAnnotation() throws Exception {

        Field field = updater.findFieldOrNull(With_IdJsonPropertyAnnotation.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("key");
    }

    @Test
    public void shouldFindObjectIdUsingFieldName() throws Exception {

        Field field = updater.findFieldOrNull(With_Id.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void shouldFindStringIdUsingFieldName() throws Exception {

        Field field = updater.findFieldOrNull(With_IdAsString.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void shouldFindObjectIdInParent() throws Exception {

        Field field = updater.findFieldOrNull(WithParent.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void whenNoObjectdIdShouldReturnNull() throws Exception {

        assertThat(updater.hasObjectId(new Coordinate(1, 1))).isFalse();
    }

    @Test
    public void whenNoDeclaredFieldShouldReturnNull() throws Exception {

        assertThat(updater.hasObjectId(new Object())).isFalse();
    }

    @Test
    public void shouldIgnoreOtherObjectId() throws Exception {

        assertThat(updater.hasObjectId(new WithOtherOid())).isFalse();
    }

    @Test
    public void shouldIgnorePojoWithoutObjectId() throws Exception {

        assertThat(updater.hasObjectId(new WithoutId())).isFalse();
    }

    @Test
    public void shouldIgnoreOtherAnnotatedObjectId() throws Exception {

        assertThat(updater.hasObjectId(new WithJsonPropertyOnAnotherField())).isFalse();
    }

    @Test
    public void shouldIgnoreOtherCustomId() throws Exception {

        assertThat(updater.hasObjectId(new WithCustomId())).isFalse();
    }

    @Test
    public void shouldSetObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        Friend friend = new Friend();

        updater.setObjectId(friend, oid);

        assertThat(friend.getId()).isEqualTo(oid);
    }

    @Test
    public void shouldSetStringIdWithObjectIdValue() throws Exception {

        ObjectId oid = new ObjectId();
        With_IdAsString target = new With_IdAsString();

        updater.setObjectId(target, oid);

        assertThat(target._id).isEqualTo(oid.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotSetCustomId() throws Exception {
        updater.setObjectId(new WithCustomId(), new ObjectId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotSetNoOID() throws Exception {
        updater.setObjectId(new WithoutId(), new ObjectId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotHandleObject() throws Exception {
        updater.setObjectId(new Object(), ObjectId.get());
    }

    private static class WithIdAnnotation {
        @Id
        ObjectId key;
    }

    private static class With_IdJsonPropertyAnnotation {
        @JsonProperty("_id")
        ObjectId key;
    }

    private static class WithJsonPropertyOnAnotherField {
        @JsonProperty("otherKey")
        ObjectId key;
    }

    private static class With_Id {
        ObjectId _id;
    }

    private static class With_IdAsString {
        String _id;
    }

    private static class WithCustomId {
        Integer _id;
    }

    private static class WithOtherOid {
        ObjectId otherKey;
    }

    private static class WithoutId {
        String value;
    }

    private static class WithParent extends With_Id {

    }
}
