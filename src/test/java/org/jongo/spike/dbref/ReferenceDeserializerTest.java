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

import com.mongodb.DB;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.jongo.spike.dbref.jackson.Reference;
import org.jongo.spike.dbref.jackson.ReferenceDeserializer;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ReferenceDeserializerTest {

    @Test
    public void shouldDeserializeBSONDBRef() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        DB db = mock(DB.class);
        SimpleModule module = new SimpleModule("dbRefModule", new Version(1, 0, 0, null));
        module.addDeserializer(Reference.class, new ReferenceDeserializer(mapper, db));
        mapper.registerModule(module);

        Reference reference = mapper.readValue("{\"$ref\":\"aCollection\",\"$id\":{\"$oid\":\"4f916b11e4b03bf323284f86\"}}", Reference.class);

        DBRef dbRef = reference.getDbRef();
        assertThat(dbRef.getDB()).isEqualTo(db);
        assertThat(dbRef.getRef()).isEqualTo("aCollection");
        assertThat(dbRef.getId()).isEqualTo(new ObjectId("4f916b11e4b03bf323284f86"));
    }


}
