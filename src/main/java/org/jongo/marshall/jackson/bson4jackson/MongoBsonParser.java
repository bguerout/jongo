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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.IOContext;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.Decimal128;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;
import org.bson.types.BSONTimestamp;

import java.io.IOException;
import java.io.InputStream;

class MongoBsonParser extends BsonParser {

    public MongoBsonParser(IOContext ctxt, int jsonFeatures, int bsonFeatures, InputStream in) {
        super(ctxt, jsonFeatures, bsonFeatures, in);
    }

    @Override
    public Object getEmbeddedObject() throws IOException, JsonParseException {
        Object object = super.getEmbeddedObject();
        if (object instanceof ObjectId) {
            return convertToNativeObjectId((ObjectId) object);
        }
        if (object instanceof Timestamp) {
            return convertToBSONTimestamp((Timestamp) object);
        }
        if (object instanceof Decimal128) {
            return convertToNativeDecimal128((Decimal128) object);
        }
        return object;
    }

    private Object convertToBSONTimestamp(Timestamp ts) {
        return new BSONTimestamp(ts.getTime(), ts.getInc());
    }

    private org.bson.types.ObjectId convertToNativeObjectId(ObjectId id) {
        return new org.bson.types.ObjectId(id.getTime(), id.getInc());
    }

    private org.bson.types.Decimal128 convertToNativeDecimal128(Decimal128 decimal) {
        return new org.bson.types.Decimal128(decimal.bigDecimalValue());
    }
}
