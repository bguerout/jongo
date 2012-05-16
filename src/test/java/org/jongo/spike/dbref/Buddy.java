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

package org.jongo.spike.dbref;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jongo.spike.dbref.jackson.DBRefDeserializer;
import org.jongo.spike.dbref.jackson.DBRefSerializer;

class Buddy {
    @JsonProperty("_id")
    ObjectId id;
    String name;
    @JsonDeserialize(using = DBRefDeserializer.class)
    @JsonSerialize(using = DBRefSerializer.class)
    Buddy friend;

    public Buddy(String name, Buddy friend) {
        this.friend = friend;
        this.name = name;
    }

    Buddy() {
    }
}
