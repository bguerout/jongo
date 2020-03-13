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

package org.jongo.marshall.jackson.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Mapping {

    private ObjectMapper mapper;
    private ReaderCallback readerCallback;
    private WriterCallback writerCallback;

    public Mapping(ObjectMapper mapper, ReaderCallback readerCallback, WriterCallback writerCallback) {
        this.mapper = mapper;
        this.readerCallback = readerCallback;
        this.writerCallback = writerCallback;
    }

    public ObjectReader getReader(Class<?> clazz) {
        return readerCallback.getReader(mapper, clazz);
    }

    public ObjectWriter getWriter(Object pojo) {
        return writerCallback.getWriter(mapper, pojo);
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static Mapping defaultMapping() {
        return new Builder().build();
    }

    public static class Builder extends AbstractMappingBuilder<Builder> {

        public Builder() {
            super();
        }

        public Builder(ObjectMapper mapper) {
            super(mapper);
        }

        @Override
        protected Builder getBuilderInstance() {
            return this;
        }

        public Mapping build() {
            return createMapping();
        }
    }
}
