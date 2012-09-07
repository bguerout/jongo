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

package org.jongo.marshall.stream;

import com.mongodb.LazyDBObject;
import org.bson.LazyBSONCallback;

class ByteArrayBsonStream extends LazyDBObject implements BsonStream {

    private final int offset;

    ByteArrayBsonStream(byte[] data, int offset, LazyBSONCallback cbk) {
        super(data, offset, cbk);
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return getBSONSize();
    }

    public byte[] getData() {
        return _input.array();
    }
}
