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

package org.jongo.marshall.jackson.bson4jackson;

import java.util.Date;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.BsonParser;

public class BsonModule extends de.undercouch.bson4jackson.BsonModule {

    public static JsonFactory createFactory() {
        BsonFactory factory = new MongoBsonFactory();
        factory.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH);
        return factory;
    }

    /**
     * TODO faster factory, must only be used during unmarshalling
     */
    public static JsonFactory createStreamingFactory() {
        BsonFactory factory = new MongoBsonFactory();
        factory.enable(BsonGenerator.Feature.ENABLE_STREAMING);
        return factory;
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addSerializers(new BsonSerializers());
        context.addDeserializers(new BsonDeserializers());
    }

    private static class BsonSerializers extends SimpleSerializers {
        public BsonSerializers() {
            addSerializer(org.bson.types.ObjectId.class, new BsonObjectIdSerializer());
        }
    }

    private static class BsonDeserializers extends SimpleDeserializers {

        public BsonDeserializers() {
            addDeserializer(Date.class, new DateDeserializer());
        }
    }

}
