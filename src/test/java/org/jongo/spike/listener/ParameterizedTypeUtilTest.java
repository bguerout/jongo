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

package org.jongo.spike.listener;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ParameterizedTypeUtilTest {

    private final ParameterizedTypeUtil util = new ParameterizedTypeUtil();

    @Test
    public void canFindTypeInInterface() throws Exception {

        EventListener<String> listener = new CustomEventListener();

        assertThat(util.getParameterizedType(listener)).isEqualTo(String.class);
    }

    @Test
    public void canFindTypeInInterfaces() throws Exception {

        EventListener<String> listener = new ManyInterfacesEventListener();

        assertThat(util.getParameterizedType(listener)).isEqualTo(String.class);
    }

    @Test
    public void canFindTypeInManyTypedInterfaces() throws Exception {

        EventListener<String> listener = new ManyTypedInterfacesEventListener();

        assertThat(util.getParameterizedType(listener)).isEqualTo(String.class);
    }

    @Test
    public void canFindTypeInParent() throws Exception {

        EventListener<Number> listener = new ListenerWithTypedParent();

        assertThat(util.getParameterizedType(listener)).isEqualTo(Number.class);
    }

    @Test
    public void canFindTypeFromUntypedParent() throws Exception {

        EventListener<String> listener = new ListenerWithUntypedParent();

        assertThat(util.getParameterizedType(listener)).isEqualTo(String.class);
    }


    private static class CustomEventListener implements EventListener<String> {
    }

    private static class ManyInterfacesEventListener implements Runnable, EventListener<String> {

        public void run() {
        }
    }

    private static class ManyTypedInterfacesEventListener implements Comparable<Number>, EventListener<String> {

        public int compareTo(Number o) {
            return 0;
        }

        public void run() {
        }
    }

    private static class UntypedParentEventListener<T> implements EventListener<T> {
    }

    private static class ListenerWithUntypedParent extends UntypedParentEventListener<String> {
    }

    private static class TypedParentEventListener implements EventListener<Number> {
    }

    private static class ListenerWithTypedParent extends TypedParentEventListener {
    }

}
