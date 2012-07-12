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

package org.jongo.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBEncoder;
import com.mongodb.DefaultDBEncoder;
import org.bson.BSONObject;
import org.bson.io.BasicOutputBuffer;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FromStringUnmarshaller implements Unmarshaller {

    private final Unmarshaller unmarshaller;

    public FromStringUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public <T> T unmarshall(byte[] data, int offset, Class<T> clazz) throws MarshallingException {
        return unmarshaller.unmarshall(data, offset, clazz);

    }

    public <T> T unmarshall(String json, Class<T> clazz) throws MarshallingException {
        try {
            BasicOutputBuffer buffer = new BasicOutputBuffer();
            DBEncoder dbEncoder = DefaultDBEncoder.FACTORY.create();
            dbEncoder.writeObject(buffer, toBsonObject(json));
            return unmarshaller.unmarshall(buffer.toByteArray(), 0, clazz);
        } catch (IOException e) {
            throw new MarshallingException("Unable to unmarshall in UTF-8", e);
        }
    }

    private BSONObject toBsonObject(String json) throws IOException, JsonParseException {
        Map map = new ObjectMapper().readValue(json, HashMap.class);
        BSONObject bson = new BasicDBObject();
        bson.putAll(map);
        return bson;
    }
}
