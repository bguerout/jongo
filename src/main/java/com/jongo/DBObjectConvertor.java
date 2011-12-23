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
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class DBObjectConvertor {

    private final ObjectMapper mapper;

    public DBObjectConvertor() {
        this.mapper = ObjectMapperFactory.createConfLessMapper();
    }

    public DBObject convert(String jsonQuery) {
        return ((DBObject) JSON.parse(jsonQuery));
    }

    public DBObject convert(Object obj) throws IOException {
        Writer writer = new StringWriter();
        mapper.writeValue(writer, obj);
        return convert(writer.toString());
    }
}
