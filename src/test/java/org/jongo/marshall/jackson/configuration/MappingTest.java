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
import org.bson.types.ObjectId;
import org.jongo.model.Friend;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingTest {

    @Test
    public void shouldNotAddBsonConfWithCustomMapper() throws Exception {
        Mapping.Builder builder = new Mapping.Builder(new ObjectMapper());
        Mapping mapping = builder.build();
        ObjectId id = ObjectId.get();//serialized using bson serializer
        Friend friend = new Friend(id, "John");

        Writer writer = new StringWriter();
        mapping.getWriter(friend).writeValue(writer, friend);

        assertThat(writer.toString()).contains("John");
    }
}
