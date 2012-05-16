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

import com.mongodb.DBObject;
import com.mongodb.DBRefBase;
import org.bson.types.*;

import java.util.*;
import java.util.regex.Pattern;

public class BSONPrimitives {
    public static final Set<Class<?>> primitives;

    static {
        primitives = new HashSet<Class<?>>();
        primitives.add(String.class);
        primitives.add(Number.class);
        primitives.add(Boolean.class);
        primitives.add(Date.class);
        primitives.add(Iterable.class);
        primitives.add(ObjectId.class);
        primitives.add(DBObject.class);
        primitives.add(Map.class);
        primitives.add(DBRefBase.class);
        primitives.add(Pattern.class);
        primitives.add(BSONTimestamp.class);
        primitives.add(UUID.class);
        primitives.add(CodeWScope.class);
        primitives.add(Code.class);
        primitives.add(MinKey.class);
        primitives.add(MaxKey.class);
        primitives.add(byte[].class);
        primitives.add(Binary.class);
    }

    public static <T> boolean contains(Class<T> clazz) {
        if (primitives.contains(clazz))
            return true;

        for (Class<?> primitive : primitives) {
            if (primitive.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}
