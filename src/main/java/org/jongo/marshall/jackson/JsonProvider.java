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

import com.mongodb.DBObject;
import org.jongo.ObjectIdUpdater;
import org.jongo.Provider;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.QueryMarshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.MappingConfig;
import org.jongo.marshall.jackson.configuration.MappingConfigBuilder;

public class JsonProvider implements Provider {

    private final JsonEngine engine;
    private final QueryMarshaller qMarshaller;

    public JsonProvider() {
        this(MappingConfigBuilder.usingJson().build());
    }

    public JsonProvider(MappingConfig config) {
        engine = new JsonEngine(config);

        Marshaller<Object, String> parameterMarshaller = new JacksonParameterMarshaller(config);
        Marshaller<String, DBObject> queryMarshaller = new JacksonQueryMarshaller();
        qMarshaller = new QueryMarshaller(parameterMarshaller, queryMarshaller);
    }

    public Marshaller<Object, DBObject> getMarshaller() {
        return engine;
    }

    public QueryMarshaller getQMarshaller() {
        return qMarshaller;
    }

    public Unmarshaller getUnmarshaller() {
        return engine;
    }

    public ObjectIdUpdater getObjectIdUpdater() {
        return new JacksonObjectIdUpdater();
    }
}
