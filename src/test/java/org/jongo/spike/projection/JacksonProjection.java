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

package org.jongo.spike.projection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bson.BsonDocumentWrapper;
import org.bson.conversions.Bson;
import org.jongo.query.Query;

import java.util.Iterator;
import java.util.Map;

public class JacksonProjection implements Projection {

    private final ObjectMapper mapper;

    public JacksonProjection(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Query getProjectionQuery(Class<?> pojoClass) {
        DBObject query = new BasicDBObject();
        putFieldNames(query, getSchemaNode(pojoClass));
        query.removeField("_id");
        return new ProjectionQuery(query);
    }

    private ObjectNode getSchemaNode(Class<?> pojoClass) {
        try {
            JsonSchema schema = mapper.generateJsonSchema(pojoClass);
            return schema.getSchemaNode();
        } catch (Exception e) {
            throw new RuntimeException("Unable to createLazyDBObject field DBObject for class: " + pojoClass, e);
        }
    }

    private void putFieldNames(DBObject dbo, JsonNode node) {
        JsonNode properties = node.get("properties");
        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode value = field.getValue();
            if (value.has("properties")) {
                BasicDBObject children = new BasicDBObject();
                putFieldNames(children, value);
                dbo.put(fieldName, children);
            } else {
                dbo.put(fieldName, 1);
            }
        }
    }

    private static class ProjectionQuery implements Query {

        private final DBObject dbo;

        private ProjectionQuery(DBObject dbo) {
            this.dbo = dbo;
        }

        public DBObject toDBObject() {
            return dbo;
        }

        public Bson toBson() {
            return BsonDocumentWrapper.asBsonDocument(dbo, MongoClient.getDefaultCodecRegistry());
        }
    }
}
