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

package org.jongo.model;

import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.ArrayList;
import java.util.List;

public class TypeWithNested {
    public static class NestedDocument {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public NestedDocument withName(String name) {
            this.name = name;
            return this;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public NestedDocument withValue(String value) {
            this.value = value;
            return this;
        }
    }

    @MongoObjectId
    private String id;
    private List<NestedDocument> nested = new ArrayList<NestedDocument>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TypeWithNested withId(String id) {
        this.id = id;
        return this;
    }

    public List<NestedDocument> getNested() {
        return nested;
    }

    public void setNested(List<NestedDocument> nested) {
        this.nested = nested;
    }

    public TypeWithNested addNested(NestedDocument nested) {
        this.nested.add(nested);
        return this;
    }
}
