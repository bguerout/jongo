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

package org.jongo.util.compatibility;

import org.jongo.Mapper;
import org.jongo.util.JongoTestCase;

import java.util.ArrayList;
import java.util.List;

public class TestContext {

    private final String contextName;
    private final Mapper mapper;
    private final List<Class<? extends JongoTestCase>> ignoredTestCases;
    private final List<String> ignoredMethods;

    public TestContext(String contextName, Mapper mapper, List<Class<? extends JongoTestCase>> ignoredTestCases, List<String> ignoredTests) {
        this.contextName = contextName;
        this.mapper = mapper;
        this.ignoredTestCases = ignoredTestCases;
        this.ignoredMethods = ignoredTests;
    }

    public TestContext(String contextName, Mapper mapper) {
        this.contextName = contextName;
        this.mapper = mapper;
        this.ignoredTestCases = new ArrayList<Class<? extends JongoTestCase>>();
        this.ignoredMethods = new ArrayList<String>();
    }

    public Mapper getMapper() {
        return mapper;
    }

    public String getContextName() {
        return contextName;
    }

    public boolean mustIgnoreTestCase(Class<?> clazz) {
        return ignoredTestCases.contains(clazz);
    }

    public boolean mustIgnoreTest(String methodName) {
        return ignoredMethods.contains(methodName);
    }
}
