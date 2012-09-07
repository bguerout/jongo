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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.model.Coordinate;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.fest.assertions.Assertions.assertThat;

public class ObjectIdFieldLocatorTest {

    private ObjectIdFieldLocator locator;

    @Before
    public void setUp() throws Exception {
        locator = new ObjectIdFieldLocator();
    }

    @Test
    public void whenNoDeclaredFieldShouldReturnNull() throws Exception {

        Field field = locator.findFieldOrNull(Object.class);

        assertThat(field).isNull();
    }

    @Test
    public void whenNoObjectdIdShouldReturnNull() throws Exception {

        Field field = locator.findFieldOrNull(Coordinate.class);

        assertThat(field).isNull();
    }

    @Test
    public void shouldLocateObjectIdUsingAnnotation() throws Exception {

        Field field = locator.findFieldOrNull(WithAnnotation.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("key");
    }

    @Test
    public void shouldLocateObjectIdUsingFieldName() throws Exception {

        Field field = locator.findFieldOrNull(WithName.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void shouldLocateObjectIdInParent() throws Exception {

        Field field = locator.findFieldOrNull(WithParent.class);

        assertThat(field).isNotNull();
        assertThat(field.getName()).isEqualTo("_id");
    }

    @Test
    public void shouldNotLocateOtherObjectId() throws Exception {

        Field field = locator.findFieldOrNull(WithOtherName.class);

        assertThat(field).isNull();
    }

    @Test
    public void shouldNotLocateOtherAnnotatedObjectId() throws Exception {

        Field field = locator.findFieldOrNull(WithOtherAnnotation.class);

        assertThat(field).isNull();
    }

    @Test
    public void shouldUpdateObjectId() throws Exception {

        ObjectId oid = new ObjectId();
        WithAnnotation target = new WithAnnotation();

        locator.findFieldAndUpdate(target, oid);

        assertThat(target.key).isEqualTo(oid);
    }

    @Test
    public void whenNoIdShouldDoNothing() throws Exception {

        ObjectId oid = new ObjectId();
        WithOtherName target = new WithOtherName();
        locator.findFieldAndUpdate(target, oid);

        assertThat(target.otherKey).isNull();
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

    private static class WithOtherName {
        ObjectId otherKey;
    }

    private static class WithParent extends WithName {

    }
}
