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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeUtil {

    Type getParameterizedType(EventListener<?> listener) {

        Class<?> clazz = listener.getClass();

        while (!Object.class.equals(clazz)) {
            Type[] interfaceTypes = clazz.getGenericInterfaces();
            if (interfaceTypes.length != 0) {
                return findTypeInInterfaces(interfaceTypes);
            } else {
                Type type = findTypeInSuperclass(clazz);
                if (type != null) {
                    return type;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalArgumentException("Unable to find parameter type in class: " + clazz);
    }

    private Type findTypeInInterfaces(Type[] interfacesTypes) {
        for (Type interfaceType : interfacesTypes) {
            if (interfaceType instanceof ParameterizedType
                    && ((ParameterizedType) interfaceType).getRawType().equals(EventListener.class)) {
                return getArgument((ParameterizedType) interfaceType);
            }
        }
        throw new IllegalArgumentException("Not a parameterized class");
    }

    private Type findTypeInSuperclass(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (!(genericSuperclass instanceof Class)) {
            return getArgument((ParameterizedType) genericSuperclass);
        }
        return null;
    }

    private Type getArgument(ParameterizedType type) {
        Type[] arguments = type.getActualTypeArguments();
        return arguments[0];
    }
}