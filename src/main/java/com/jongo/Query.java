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

package com.jongo;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class Query {
    private final String query;
    private String fields;

    public Query(String query) {
        this.query = query;
    }

    public Query(String query, String fields) {
        this.query = query;
        this.fields = fields;
    }

    public DBObject toDBObject() {
        return asDBObject(query);
    }

    private DBObject asDBObject(String json) {
        return ((DBObject) JSON.parse(json));
    }

    public DBObject getFields() {
        return asDBObject(fields);
    }

    public String getQuery() {
        return query;
    }

    public static Query query(String query, Object... parameters) {
        return new Builder(query).parameters(parameters).build();
    }

    public static Query query(String query) {
        return new Builder(query).build();
    }

    public static class Builder {

        private final String template;
        private final ParameterBinder binder;
        private Object[] parameters;
        private String fields;

        public Builder(String query) {
            this(query, new ParameterBinder());
        }

        protected Builder(String query, ParameterBinder binder) {
            this.template = query;
            this.binder = binder;
        }

        public Builder parameters(Object... parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder fields(String fields) {
            this.fields = fields;
            return this;
        }

        public Query build() {
            String query = template;
            if (parameters != null) {
                query = binder.bind(template, parameters);
            }
            return new Query(query, fields);
        }
    }
}
