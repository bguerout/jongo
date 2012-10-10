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

package org.jongo.marshall.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;

import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static org.jongo.MongoCollection.MONGO_DOCUMENT_ID_NAME;

public class JacksonQueryMarshaller implements Marshaller<String, DBObject> {

    private ObjectMapper mapper;
    private final static String MONGO_QUERY_OID = "$oid";

    public JacksonQueryMarshaller() {
        mapper = new ObjectMapper();
        mapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public DBObject marshall(String query) throws MarshallingException {
        try {
            Map map = mapper.reader(Map.class).readValue(query.replace('\'', '"'));
            replaceOidWithObjectIdIfExists(map);
            return new BasicDBObject(map);
        } catch (Exception e) {
            throw new IllegalArgumentException(query + " cannot be parsed", e);
        }
    }

    private void replaceOidWithObjectIdIfExists(Map map) {
        Object _id = map.get(MONGO_DOCUMENT_ID_NAME);
        if(_id != null && _id instanceof Map) {
            Map<String, String> oid = (Map)_id;
            String value = oid.get(MONGO_QUERY_OID);
            if(value != null)
                map.put(MONGO_DOCUMENT_ID_NAME, new ObjectId(value));
        }
    }
}
