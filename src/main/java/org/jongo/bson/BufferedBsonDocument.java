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

package org.jongo.bson;

import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBEncoder;
import org.bson.BSONObject;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.OutputBuffer;

class BufferedBsonDocument implements BsonDocument {

    private final OutputBuffer buffer;
    private final DBObject dbo;

    BufferedBsonDocument(DBObject dbo) {
        this.buffer = new BasicOutputBuffer();
        this.dbo = dbo;
        encode(this.dbo);
    }

    private void encode(BSONObject dbo) {
        DBEncoder dbEncoder = DefaultDBEncoder.FACTORY.create();
        dbEncoder.writeObject(buffer, dbo);
    }

    public int getSize() {
        return buffer.size();
    }

    public byte[] toByteArray() {
        return buffer.toByteArray();
    }

    public DBObject toDBObject() {
        return dbo;
    }

    @Override
    public String toString() {
        return dbo.toString();
    }
}
