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
    public void whenNoDeclaredFieldShouldReturnNull() throws Exception {

        Field field = updater.findFieldOrNull(Object.class);

        assertThat(field).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnInvalidCall() throws Exception {

        updater.setObjectId(new Object(), ObjectId.get());
    }

    @Test
    public void whenNoObjectdIdShouldReturnNull() throws Exception {

        Field field = updater.findFieldOrNull(Coordinate.class);

        assertThat(field).isNull();
    }

    @Test
    public void shouldLocateObjectIdUsingAnnotation() throws Exception {

        Field field = updater.findFieldOrNull(WithAnnotation.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("key");
    }

    @Test
    public void shouldLocateObjectIdUsingFieldName() throws Exception {

        Field field = updater.findFieldOrNull(WithName.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void shouldLocateObjectIdInParent() throws Exception {

        Field field = updater.findFieldOrNull(WithParent.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void shouldNotLocateOtherObjectId() throws Exception {

        Field field = updater.findFieldOrNull(WithOtherName.class);

        assertThat(field).isNull();
    }

    @Test
    public void shouldNotLocateOtherAnnotatedObjectId() throws Exception {

        Field field = updater.findFieldOrNull(WithOtherAnnotation.class);

        assertThat(field).isNull();
    }

    @Test
    public void shouldUpdateObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        Friend friend = new Friend();

        updater.setObjectId(friend, oid);

        assertThat(friend.getId()).isEqualTo(oid);
    }

    @Test
    public void shouldUpdateStringIdWithObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        WithIdAsString target = new WithIdAsString();

        updater.setObjectId(target, oid);

        assertThat(target._id).isEqualTo(oid.toString());
    }

    @Test
    public void shouldNotUpdateNonSupportedId() throws Exception {

        ObjectId oid = new ObjectId();
        WithIdAsInteger target = new WithIdAsInteger();

        updater.setObjectId(target, oid);

        assertThat(target._id).isNull();
    }

    @Test
    public void canUpdateObjectId() throws Exception {

        Friend friend = new Friend();

        boolean canUpdate = updater.haveAnId(friend);

        assertThat(canUpdate).isFalse();
    }

    @Test
    public void whenFriendHasIdThenCannotSetIt() throws Exception {

        ObjectId oid = new ObjectId();
        Friend friend = new Friend(oid, "John");

        boolean canUpdate = updater.haveAnId(friend);

        assertThat(canUpdate).isTrue();
    }

    private static class WithAnnotation {
        @JsonProperty("_id")
        ObjectId key;
    }

    private static class WithOtherAnnotation {
        @JsonProperty("otherKey")
        ObjectId key;
    }

    private static class WithName {
        ObjectId _id;
    }

    private static class WithIdAsString {
        String _id;
    }

    private static class WithIdAsInteger {
        Integer _id;
    }

    private static class WithOtherName {
        ObjectId otherKey;
    }

    private static class WithParent extends WithName {

    }
}
