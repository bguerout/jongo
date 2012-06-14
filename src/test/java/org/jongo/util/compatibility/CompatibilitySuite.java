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

import static java.lang.Thread.currentThread;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.jongo.JongoTest;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class CompatibilitySuite extends Suite {

    private static final String SCANNED_PACKAGE = "org.jongo";

    private final List<Runner> runners = new ArrayList<Runner>();

    public CompatibilitySuite(Class<?> suite) throws Throwable {
        super(suite, new Class<?>[] {});

        TestContext parameter = getParameter(getTestClass());
        Class<?> tests[] = prepareMarshallingStrategy(parameter.getMarshaller(), parameter.getUnmarshaller());
        runners.addAll(new JUnit4Builder().runners(suite, tests));
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    /**
     * Marshalling strategy updating
     */
    private Class<?>[] prepareMarshallingStrategy(Marshaller marshaller, Unmarshaller unmarshaller) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        Class<?>[] suiteClasses = getSuiteClasses(SCANNED_PACKAGE);
        for (Class<?> suiteClass : suiteClasses) {
            if (suiteClass.equals(JongoTest.class)) {
                Method method = suiteClass.getMethod("prepareMarshallingStrategy", Marshaller.class, Unmarshaller.class);
                method.invoke(null, marshaller, unmarshaller);
                break;
            } else {
                classes.add(suiteClass);
            }
        }
        return classes.toArray(new Class[0]);
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

    /**
     * Package introspection
     */
    public Class<?>[] getSuiteClasses(String scannedPackage) throws ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        File directory = new File(currentThread().getContextClassLoader().getResource(scannedPackage.replace('.', '/')).getFile());
        if (directory.exists()) {
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith("Test.class"))
                    classes.add(Class.forName(scannedPackage + '.' + files[i].substring(0, files[i].length() - 6)));
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }
}
