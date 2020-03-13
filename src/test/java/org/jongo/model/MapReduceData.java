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

import org.jongo.marshall.jackson.oid.Id;

import java.util.Date;

/**
 * A class that is shaped like results from map reduce operations.
 *
 * @author Christian Trimble
 */
public class MapReduceData {

    public static class Key {
        protected String group;
        protected Date date;

        public Key() {
        }

        public Key(String group, Date date) {
            this.group = group;
            this.date = date;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    public static class Value {
        protected int count;

        public Value() {
        }

        public Value(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    @Id
    protected Key id;
    protected Value value;

    public MapReduceData() {
    }

    public MapReduceData(String group, Date date, int count) {
        this.id = new Key(group, date);
        this.value = new Value(count);
    }

    public Key getId() {
        return id;
    }

    public void setId(Key id) {
        this.id = id;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
