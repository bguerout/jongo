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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.bson.BSONObject;
import org.jongo.marshall.BsonObjectStream;
import org.jongo.marshall.DocumentStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BSON {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static DocumentStream bsonify(String json) throws IOException {
        Map map = OBJECT_MAPPER.readValue(jsonify(json), HashMap.class);
        BSONObject bson = new BasicDBObject();
        bson.putAll(map);
        return new BsonObjectStream(bson);
    }

    public static String jsonify(String json) {
        return json.replace("'", "\"");
    }
}
