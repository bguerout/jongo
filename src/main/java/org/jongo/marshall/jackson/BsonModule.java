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

import java.util.Date;

import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.jongo.marshall.BSONPrimitives;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.mongodb.DBObject;

class BsonModule extends Module {

    @Override
    public String getModuleName() {
        return "BsonModule";
    }

    @Override
    public Version version() {
        return new Version(2, 0, 0, "", "org.jongo", "bsonmodule");
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        context.addSerializers(new NativeBsonSerializers());
        context.addDeserializers(new BsonDeserializers());
    }

    private static class BsonDeserializers extends SimpleDeserializers {
        public BsonDeserializers() {
            EmbeddedObjectDeserializer deserializer = new EmbeddedObjectDeserializer();
            NativeDeserializer nativeDeserializer = new NativeDeserializer();
            addDeserializer(Date.class, new BackwardDateDeserializer(deserializer, nativeDeserializer));
            addDeserializer(MinKey.class, deserializer);
            addDeserializer(MaxKey.class, deserializer);
            addDeserializer(DBObject.class, nativeDeserializer);
        }
    }

    private static class NativeBsonSerializers extends SimpleSerializers {
        public NativeBsonSerializers() {
            NativeSerializer serializer = new NativeSerializer();
            for (Class primitive : BSONPrimitives.getPrimitives()) {
                addSerializer(primitive, serializer);
            }
        }
    }
}