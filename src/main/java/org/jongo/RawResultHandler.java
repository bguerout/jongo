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

package org.jongo;

import com.mongodb.DBObject;

public class RawResultHandler<T extends DBObject> implements ResultHandler<T> {

    private final Class<T> clazz;

    public RawResultHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public RawResultHandler() {
        this((Class<T>) DBObject.class);
    }

    public T map(DBObject result) {
        return clazz.cast(result);
    }

    public static <T extends DBObject> RawResultHandler<T> asRaw(Class<T> clazz) {
        return new RawResultHandler<T>(clazz);
    }
}
