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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BSONPrimitives
{
    private static final Set<Class<?>> primitives;
    
    static {
        primitives = new HashSet<Class<?>>();
        primitives.add(String.class);
        primitives.add(Integer.class);
        primitives.add(int.class);
        primitives.add(Double.class);
        primitives.add(double.class);
        primitives.add(Long.class);
        primitives.add(long.class);
        primitives.add(Boolean.class);
        primitives.add(boolean.class);
        primitives.add(Date.class);
    }
    
    public static <T> boolean contains(Class<T> clazz)
    {
        return primitives.contains(clazz);
    }
}
