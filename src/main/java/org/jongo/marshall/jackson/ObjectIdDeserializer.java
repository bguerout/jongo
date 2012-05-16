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

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
        String id = jp.getText();
        if (id.startsWith("{")) {
            return createObjectIdFromAField(jp, id);
        }
        return new ObjectId(id);
    }

    private ObjectId createObjectIdFromAField(JsonParser jp, String id) throws IOException {
        JsonNode oid = jp.readValueAsTree().get("$oid");
        if (oid == null) {
            throw new IllegalArgumentException("Unable to convert " + id + " into an ObjectId");
        }
        return new ObjectId(oid.getTextValue());
    }
}
