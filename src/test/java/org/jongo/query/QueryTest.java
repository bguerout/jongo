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

package org.jongo.query;

import com.mongodb.DBObject;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryTest {

    @Test
    public void shouldConvertToDBObject() throws Exception {

        Query query = new BsonQueryFactory(new JacksonEngine(Mapping.defaultMapping())).createQuery("{'value':1}");

        DBObject dbObject = query.toDBObject();

        assertThat(dbObject.containsField("value")).isTrue();
        assertThat(dbObject.get("value")).isEqualTo(1);
    }

}
