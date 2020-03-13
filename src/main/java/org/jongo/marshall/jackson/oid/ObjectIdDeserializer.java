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

package org.jongo.marshall.jackson.oid;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.bson.types.ObjectId;

import java.io.IOException;

import static org.jongo.MongoCollection.MONGO_QUERY_OID;

public class ObjectIdDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private boolean fieldIsObjectId = false;

    public ObjectIdDeserializer() {
        this(false);
    }

    public ObjectIdDeserializer(boolean fieldIsObjectId) {
        this.fieldIsObjectId = fieldIsObjectId;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        TreeNode treeNode = jp.readValueAsTree();
        JsonNode oid = ((JsonNode) treeNode).get(MONGO_QUERY_OID);
        if (fieldIsObjectId) {
            if (oid != null) {
                return new ObjectId(oid.asText());
            } else {
                return new ObjectId(((JsonNode) treeNode).asText());
            }
        } else {
            if (oid != null) {
                return oid.asText();
            } else {
                return ((JsonNode) treeNode).asText();
            }
        }
    }

    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new ObjectIdDeserializer(ObjectId.class.isAssignableFrom(property.getType().getRawClass()));
    }
}
