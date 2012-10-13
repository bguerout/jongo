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
import org.jongo.marshall.QueryMarshaller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static org.jongo.MongoCollection.MONGO_DOCUMENT_ID_NAME;

public class JacksonQueryMarshaller implements QueryMarshaller {

    private final Marshaller marshaller;
    private final ObjectMapper mapper;
    private final static String MONGO_QUERY_OID = "$oid";

    public JacksonQueryMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
        mapper = new ObjectMapper();
        mapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public String marshallParameter(Object parameter) {
        if (WRAPPERS.contains(parameter.getClass()))
            return String.valueOf(parameter);
        else if(parameter instanceof String || parameter instanceof Enum)
            return "\"" + parameter + "\"";
        else if (parameter instanceof ObjectId)
            return "{" + MONGO_QUERY_OID + ":\"" + parameter.toString() + "\"}";
        else
            return marshall(parameter);
    }

    private String marshall(Object parameter) {
        try {
            DBObject dbObject = marshaller.marshall(parameter);
            return dbObject.toString();
        } catch (Exception e) {
            String message = String.format("Unable to marshall json from: %s", parameter);
            throw new MarshallingException(message, e);
        }
    }

    private static Set WRAPPERS;
    static {
        WRAPPERS = createSet(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class);
    }

    private static Set<Class<?>> createSet(Class... classes) {
        Set<Class<?>> set = new HashSet<Class<?>>();
        for(Class<?> clazz : classes)
            set.add(clazz);
        return set;
    }

    public DBObject marshallQuery(String query) {
        try {
            Map<String, Object> map = mapper.reader(Map.class).readValue(query.replace('\'', '"'));
            findObjectIds(map);
            return new BasicDBObject(map);
        } catch (Exception e) {
            throw new IllegalArgumentException(query + " cannot be parsed", e);
        }
    }

    private void findObjectIds(Map<String, Object> map) {
        for(String key : map.keySet()) {
            Object value = map.get(key);
            if(key.equals(MONGO_DOCUMENT_ID_NAME))
                replaceObjectId(map, value);
            else if(value instanceof List)
                deepFindObjectIds(((List)value).toArray());
            else deepFindObjectIds(value);
        }
    }

    private void deepFindObjectIds(Object... values) {
        for(Object value : values)
            if (value instanceof Map)
                findObjectIds((Map<String, Object>) value);
    }

    private void replaceObjectId(Map<String, Object> map, Object _id) {
        if(_id instanceof Map) {
            Map<String, String> oid = (Map)_id;
            String value = oid.get(MONGO_QUERY_OID);
            if(value != null)
                map.put(MONGO_DOCUMENT_ID_NAME, new ObjectId(value));
        }
    }
}
