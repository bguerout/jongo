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

package org.jongo.compatibility;

import org.jongo.ObjectIdUpdater;
import org.jongo.Mapper;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.JacksonObjectIdUpdater;
import org.jongo.marshall.jackson.Mapping;
import org.jongo.query.JsonQueryFactory;
import org.jongo.query.QueryFactory;

class JsonMapper implements Mapper {

    private final JsonEngine engine;

    public JsonMapper(Mapping config) {
        engine = new JsonEngine(config);
    }

    public Marshaller getMarshaller() {
        return engine;
    }

    public Unmarshaller getUnmarshaller() {
        return engine;
    }

    public ObjectIdUpdater getObjectIdUpdater() {
        return new JacksonObjectIdUpdater();
    }

    public QueryFactory getQueryFactory() {
        return new JsonQueryFactory(engine);
    }
}
