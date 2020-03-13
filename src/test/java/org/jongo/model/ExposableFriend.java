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

import com.google.common.base.Objects;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import org.jongo.marshall.jackson.oid.ObjectId;

public class ExposableFriend {

    @Id //see NewAnnotationsCompatibilitySuiteTest for more informations
    @ObjectId //see NewAnnotationsCompatibilitySuiteTest for more informations
    @MongoId
    @MongoObjectId
    private String id;
    private String name;

    private ExposableFriend() {
        //jackson
    }

    public ExposableFriend(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ExposableFriend(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ExposableFriend)) return false;
        ExposableFriend ef = (ExposableFriend) o;

        return Objects.equal(id, ef.id) &&
                Objects.equal(name, ef.name);
    }

    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }

}
