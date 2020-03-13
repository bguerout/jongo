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

package org.jongo.util.compatibility;

import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.IgnoredClassRunner;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Modifier;
import java.util.List;

public class CompatibilitySuite extends Suite {

    public CompatibilitySuite(Class<?> suiteClass) throws Throwable {
        super(suiteClass, new Class<?>[]{});
    }

    @Override
    protected List<Runner> getChildren() {
        try {
            TestContext testContext = getTestContextFromParameter();
            CompatibilitySuiteRunner builder = new CompatibilitySuiteRunner(testContext);

            return builder.runners(getTestClass().getJavaClass(), testContext.findTestCases());
        } catch (Throwable cause) {
            throw new RuntimeException("Unable to create runners for this suite", cause);
        }
    }

    private TestContext getTestContextFromParameter() throws Throwable {
        return (TestContext) getParametersMethod(getTestClass()).invokeExplosively(null);
    }

    private FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Parameterized.Parameters.class);
        for (FrameworkMethod each : methods) {
            int modifiers = each.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
                return each;
        }
        throw new Exception("No public static parameters method on class " + testClass.getName());
    }

    private static class CompatibilitySuiteRunner extends JUnit4Builder {

        private TestContext testContext;

        public CompatibilitySuiteRunner(TestContext testContext) {
            this.testContext = testContext;
        }

        @Override
        public Runner runnerForClass(Class<?> testClass) throws Throwable {
            if (testContext.mustIgnoreTestCase(testClass)) {
                return new IgnoredClassRunner(testClass);
            }
            RunWith annotation = testClass.getAnnotation(RunWith.class);
            if (annotation != null) {
                return new AnnotatedBuilder(this).runnerForClass(testClass);
            }
            return new JongoTestClassRunner(testClass, testContext);
        }
    }

    private static class JongoTestClassRunner extends BlockJUnit4ClassRunner {

        private TestContext testContext;

        public JongoTestClassRunner(Class<?> aClass, TestContext testContext) throws InitializationError {
            super(aClass);
            this.testContext = testContext;
        }

        @Override
        protected String getName() {
            return testContext.getContextName() + "-" + super.getName();
        }

        @Override
        protected Object createTest() throws Exception {
            Object testCase = super.createTest();
            testContext.prepareTestCase(testCase);
            return testCase;
        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {
            if (testContext.mustIgnoreTest(method.getMethod().getDeclaringClass(), method.getName())) {
                notifier.fireTestIgnored(describeChild(method));
            } else {
                super.runChild(method, notifier);
            }
        }
    }
}
