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

package org.jongo.marshall.decoder;

import com.mongodb.LazyDBObject;
import org.bson.LazyBSONCallback;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;

public class ReadOnlyDBObject<T> extends LazyDBObject {

    private final int offset;
    private final Unmarshaller unmarshaller;

    ReadOnlyDBObject(byte[] data, int offset, LazyBSONCallback cbk, Unmarshaller unmarshaller) {
        super(data, offset, cbk);
        this.offset = offset;
        this.unmarshaller = unmarshaller;
    }

    public T as(Class<T> clazz) {
        try {
            return unmarshaller.unmarshall(_input.array(), offset, clazz);
        } catch (MarshallingException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, toString());
            throw new MarshallingException(message, e);
        }

    }
}
