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
import org.jongo.ReflectiveObjectIdUpdater;
import org.jongo.ObjectIdUpdater;
import org.jongo.Mapper;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.AbstractMappingBuilder;
import org.jongo.query.JsonQueryFactory;
import org.jongo.query.QueryFactory;

public class JacksonMapper implements Mapper {

    private final JacksonEngine engine;
    private final ObjectIdUpdater objectIdUpdater;
    private final QueryFactory queryFactory;

    private JacksonMapper(JacksonEngine engine, QueryFactory queryFactory, ObjectIdUpdater objectIdUpdater) {
        this.engine = engine;
        this.queryFactory = queryFactory;
        this.objectIdUpdater = objectIdUpdater;
    }

    public Marshaller getMarshaller() {
        return engine;
    }

    public Unmarshaller getUnmarshaller() {
        return engine;
    }

    public ObjectIdUpdater getObjectIdUpdater() {
        return objectIdUpdater;
    }

    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    public static class Builder extends AbstractMappingBuilder<Builder> {

        private QueryFactory queryFactory;
        private ObjectIdUpdater objectIdUpdater;

        public Builder() {
            super();
        }

        public Builder(ObjectMapper mapper) {
            super(mapper);
        }

        public Mapper build() {
            JacksonEngine jacksonEngine = new JacksonEngine(innerMapping());
            if (queryFactory == null) {
                queryFactory = new JsonQueryFactory(jacksonEngine);
            }
            if (objectIdUpdater == null) {
                objectIdUpdater = new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector());
            }
            return new JacksonMapper(jacksonEngine, queryFactory, objectIdUpdater);
        }

        public Builder withQueryFactory(QueryFactory factory) {
            this.queryFactory = factory;
            return getBuilderInstance();
        }

        public Builder withObjectIdUpdater(ObjectIdUpdater objectIdUpdater) {
            this.objectIdUpdater = objectIdUpdater;
            return getBuilderInstance();
        }

        @Override
        protected Builder getBuilderInstance() {
            return this;
        }
    }
}
