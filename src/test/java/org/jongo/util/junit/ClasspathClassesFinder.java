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

package org.jongo.util.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class that will search, in the classpath, all classes annotated with a given annotation (normally @SlowTest or @FastTest) and that belongs
 * to a given package name. Note that this package parameter is not mandatory, but just used to limit the searches.
 *
 * @author Romain Linsolas
 * @version 1.0
 * @date 16/02/2011
 */
public final class ClasspathClassesFinder {

    public static final Class<? extends Annotation> IGNORE_ANNOTATION = null;

    /**
     * Get the list of classes of a given package name
     *
     * @param packageName The package name of the classes.
     * @return The List of classes that matches the requirements.
     */
    public static Class<?>[] getSuiteClasses(String packageName) {
        return getSuiteClasses(packageName, IGNORE_ANNOTATION);
    }

    /**
     * Get the list of classes of a given package name, and that are annotated by a given annotation.
     *
     * @param packageName    The package name of the classes.
     * @param testAnnotation The annotation the class should be annotated with.
     * @return The List of classes that matches the requirements.
     */
    public static Class<?>[] getSuiteClasses(String packageName, Class<? extends Annotation> testAnnotation) {
        try {
            return getClasses(packageName, testAnnotation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the list of classes of a given package name, and that are annotated by a given annotation.
     *
     * @param packageName The package name of the classes.
     * @param annotation  The annotation the class should be annotated with.
     * @return The List of classes that matches the requirements.
     * @throws ClassNotFoundException If something goes wrong...
     * @throws java.io.IOException    If something goes wrong...
     */
    private static Class<?>[] getClasses(String packageName, Class<? extends Annotation> annotation) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        // Get classpath
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        // For each classpath, get the classes.
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, annotation));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Find classes, in a given directory (recursively), for a given package name, that are annotated by a given annotation.
     *
     * @param directory   The directory where to look for.
     * @param packageName The package name of the classes.
     * @param annotation  The annotation the class should be annotated with.
     * @return The List of classes that matches the requirements.
     * @throws ClassNotFoundException If something goes wrong...
     */
    private static List<Class<?>> findClasses(File directory, String packageName, Class<? extends Annotation> annotation)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName(), annotation));
            } else if (file.getName().endsWith("Test.class")) {
                // We remove the .class at the end of the filename to get the class name...
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if ((annotation == null || clazz.getAnnotation(annotation) != null) && isANotTestSuite(clazz)) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }

    private static boolean isANotTestSuite(Class<?> clazz) {
        RunWith runWith = clazz.getAnnotation(RunWith.class);
        return runWith == null || runWith.value().isAssignableFrom(Suite.class);
    }

}
