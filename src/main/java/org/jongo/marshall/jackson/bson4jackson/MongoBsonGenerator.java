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

package org.jongo.marshall.jackson.bson4jackson;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.io.ByteOrderUtil;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;

import java.io.IOException;
import java.io.OutputStream;

class MongoBsonGenerator extends BsonGenerator {

    public MongoBsonGenerator(int jsonFeatures, int bsonFeatures, OutputStream out) {
        super(jsonFeatures, bsonFeatures, out);
    }

    public void writeNativeObjectId(org.bson.types.ObjectId objectId) throws IOException {
        _writeArrayFieldNameIfNeeded();
        _verifyValueWrite("write datetime");
        _buffer.putByte(_typeMarker, BsonConstants.TYPE_OBJECTID);
        // ObjectIds have their byte order flipped
        int time = ByteOrderUtil.flip(Long.valueOf(objectId.getTime() / 1000L).intValue());
        int machine = ByteOrderUtil.flip(objectId.getMachine());
        int inc = ByteOrderUtil.flip(objectId.getInc());
        _buffer.putInt(time);
        _buffer.putInt(machine);
        _buffer.putInt(inc);
        flushBuffer();
    }

    public void writeBSONTimestamp(BSONTimestamp timestamp) throws IOException {
        _writeArrayFieldNameIfNeeded();
        _verifyValueWrite("write timestamp");
        _buffer.putByte(_typeMarker, BsonConstants.TYPE_TIMESTAMP);
        _buffer.putInt(timestamp.getInc());
        _buffer.putInt(timestamp.getTime());
        flushBuffer();
    }

    public void writeMinKey(MinKey key) throws IOException {
        _writeArrayFieldNameIfNeeded();
        _verifyValueWrite("write int");
        _buffer.putByte(_typeMarker, BsonConstants.TYPE_MINKEY);
        flushBuffer();
    }

    public void writeMaxKey(MaxKey key) throws IOException {
        _writeArrayFieldNameIfNeeded();
        _verifyValueWrite("write boolean");
        _buffer.putByte(_typeMarker, BsonConstants.TYPE_MAXKEY);
        flushBuffer();
    }

    public void writeBinary(Binary binary) throws IOException {
        _writeArrayFieldNameIfNeeded();
        _verifyValueWrite("write binary");
        byte[] bytes = binary.getData();
        _buffer.putByte(_typeMarker, BsonConstants.TYPE_BINARY);
        _buffer.putInt(bytes.length);
        _buffer.putByte(binary.getType());
        _buffer.putBytes(binary.getData());
        flushBuffer();
    }
}
