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

package org.jongo.marshall.jackson.bson4jackson;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonGenerator;
import org.bson.types.*;

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
        _buffer.putBytes(objectId.toByteArray());
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

    public void writeDecima128(Decimal128 decimal) throws IOException {
        _writeArrayFieldNameIfNeeded();
        _verifyValueWrite("write number");
        _buffer.putByte(_typeMarker, BsonConstants.TYPE_DECIMAL128);
        _buffer.putLong(decimal.getLow());
        _buffer.putLong(decimal.getHigh());
        flushBuffer();
    }
}
