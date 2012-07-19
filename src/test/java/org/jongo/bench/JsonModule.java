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

package org.jongo.bench;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.mongodb.util.JSON;

class JsonModule extends Module {

    @Override
    public String getModuleName() {
        return "JongoModule";
    }

    @Override
    public Version version() {
        return new Version(2, 0, 0, "", "org.jongo", "jongomodule");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new JsonSerializers());
    }

    private static class JsonSerializers extends SimpleSerializers {
        public JsonSerializers() {
            addSerializer(org.bson.types.ObjectId.class, new JsonObjectIdSerializer());
        }
    }

    private static class JsonObjectIdSerializer extends JsonSerializer<ObjectId> {
        public void serialize(ObjectId obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeRawValue(JSON.serialize(obj));
        }
    }
}