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

import org.jongo.util.JongoTestCase;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class CompatibilitySuite extends Suite {

    private static final String SCANNED_PACKAGE = "org.jongo";
    private static final Class<JongoTestCase> PARENT_CLASS = JongoTestCase.class;

    private final List<Runner> runners = new ArrayList<Runner>();
    private final ContextRunnerBuilder builder;

    public CompatibilitySuite(Class<?> clazz) throws Throwable {
        super(clazz, new Class<?>[]{});
        builder = new ContextRunnerBuilder(getParameter(getTestClass()));
        Class<?>[] suiteClasses = ClasspathClassesFinder.getSuiteClasses(SCANNED_PACKAGE);
        runners.addAll(builder.runners(clazz, suiteClasses));
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    /**
     * @see Parameterized
     */
    private TestContext getParameter(TestClass klass) throws Throwable {
        return (TestContext) getParametersMethod(klass).invokeExplosively(null);
    }

    /**
     * @see Parameterized
     */
    private FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Parameterized.Parameters.class);
        for (FrameworkMethod each : methods) {
            int modifiers = each.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
                return each;
        }
        throw new Exception("No public static parameters method on class " + testClass.getName());
    }

    private static class ContextRunnerBuilder extends JUnit4Builder {

        private TestContext testContext;

        public ContextRunnerBuilder(TestContext testContext) {
            this.testContext = testContext;
        }

        @Override
        public Runner runnerForClass(Class<?> testClass) throws Throwable {
            return new ContextJUnit4ClassRunner(testClass, testContext);
        }
    }

    private static class ContextJUnit4ClassRunner extends BlockJUnit4ClassRunner {

        private TestContext testContext;

        public ContextJUnit4ClassRunner(Class<?> aClass, TestContext testContext) throws InitializationError {
            super(aClass);
            this.testContext = testContext;
        }

        @Override
        protected Object createTest() throws Exception {
            Object test = super.createTest();
            if (PARENT_CLASS.isAssignableFrom(getTestClass().getJavaClass())) {
                JongoTestCase jongoTestCase = (JongoTestCase) test;
                jongoTestCase.prepareMarshallingStrategy(testContext.getMarshaller(), testContext.getUnmarshaller());
            }
            return test;
        }
    }
}
