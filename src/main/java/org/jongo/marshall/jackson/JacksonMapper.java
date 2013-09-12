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
import org.bson.types.ObjectId;
import org.jongo.MapObjectIdUpdater;
import org.jongo.Mapper;
import org.jongo.ObjectIdUpdater;
import org.jongo.ReflectiveObjectIdUpdater;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.marshall.jackson.configuration.AbstractMappingBuilder;
import org.jongo.query.BsonQueryFactory;
import org.jongo.query.QueryFactory;

import java.util.HashMap;
import java.util.Map;

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
        private final Map<Class<?>, ObjectIdUpdater<?>> updaters = new HashMap<Class<?>, ObjectIdUpdater<?>>();

        public Builder() {
            super();
        }

        public Builder(ObjectMapper mapper) {
            super(mapper);
        }

        public Mapper build() {
            JacksonEngine jacksonEngine = new JacksonEngine(createMapping());
            setDefaultsIfNeeded(jacksonEngine);
            return new JacksonMapper(jacksonEngine, queryFactory, objectIdUpdater);
        }

        private void setDefaultsIfNeeded(JacksonEngine jacksonEngine) {
            if (queryFactory == null) {
                queryFactory = new BsonQueryFactory(jacksonEngine);
            }
            if (objectIdUpdater == null) {
                objectIdUpdater = createDefaultObjectIdUpdaters();
            }
        }

        private ObjectIdUpdater<?> createDefaultObjectIdUpdaters() {
            ReflectiveObjectIdUpdater defaultObjectIdUpdater = new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector());
            updaters.put(Map.class, new MapObjectIdUpdater());
            return new MultiObjectIdUpdater(defaultObjectIdUpdater, updaters);
        }

        public Builder withQueryFactory(QueryFactory factory) {
            this.queryFactory = factory;
            return getBuilderInstance();
        }

        public Builder withObjectIdUpdater(ObjectIdUpdater objectIdUpdater) {
            this.objectIdUpdater = objectIdUpdater;
            return getBuilderInstance();
        }

        public <T> Builder registerObjectIdUpdater(Class<T> clazz, ObjectIdUpdater<T> objectIdUpdater) {
            updaters.put(clazz, objectIdUpdater);
            return getBuilderInstance();
        }

        @Override
        protected Builder getBuilderInstance() {
            return this;
        }
    }

    private static class MultiObjectIdUpdater implements ObjectIdUpdater {

        private final Map<Class<?>, ObjectIdUpdater<?>> updaters;
        private final ObjectIdUpdater defaultObjectIdUpdater;

        private MultiObjectIdUpdater(ObjectIdUpdater defaultObjectIdUpdater, Map<Class<?>, ObjectIdUpdater<?>> updaters) {
            this.updaters = updaters;
            this.defaultObjectIdUpdater = defaultObjectIdUpdater;
        }

        public boolean mustGenerateObjectId(Object pojo) {
            return findUpdater(pojo.getClass()).mustGenerateObjectId(pojo);
        }

        public void setObjectId(Object obj, ObjectId id) {
            findUpdater(obj.getClass()).setObjectId(obj, id);
        }

        public Object getId(Object obj) {
            return findUpdater(obj.getClass()).getId(obj);
        }

        private ObjectIdUpdater findUpdater(Class<?> clazz) {
            for (Class<?> registeredClass : updaters.keySet()) {
                if (registeredClass.isAssignableFrom(clazz)) {
                    return updaters.get(registeredClass);
                }
            }
            return defaultObjectIdUpdater;
        }
    }
}
