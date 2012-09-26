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

import org.jongo.Provider;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.MappingConfig;
import org.jongo.marshall.jackson.configuration.MappingConfigBuilder;
import org.jongo.query.QueryBinder;

public class JacksonProvider implements Provider {

    private final JsonProcessor processor;
    private final JacksonQueryBinder queryBinder;

    public JacksonProvider() {
        MappingConfig config = MappingConfigBuilder.usingJson().createConfiguration();
        processor = new JsonProcessor(config);
        queryBinder = new JacksonQueryBinder(config);
    }

    public Marshaller getMarshaller() {
        return processor;
    }

    public Unmarshaller getUnmarshaller() {
        return processor;
    }

    public QueryBinder getQueryBinder() {
        return queryBinder;
    }
}
