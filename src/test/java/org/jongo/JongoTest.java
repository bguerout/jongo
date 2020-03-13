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

package org.jongo;

import org.jongo.model.Friend;
import org.jongo.query.Query;
import org.jongo.util.JongoTestBase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JongoTest extends JongoTestBase {

    @Test
    public void canObtainACollection() throws Exception {

        Jongo jongo = new Jongo(getDatabase());

        MongoCollection collection = jongo.getCollection("collection-name");

        assertThat(collection).isNotNull();
        assertThat(collection.getName()).isEqualTo("collection-name");
    }

    @Test
    public void canCreateQuery() throws Exception {

        Jongo jongo = new Jongo(getDatabase());

        Query query = jongo.createQuery("{test:1}");

        assertThat(query.toDBObject().get("test")).isEqualTo(1);
    }

    @Test
    public void canGetMapper() throws Exception {

        Jongo jongo = new Jongo(getDatabase());

        Mapper mapper = jongo.getMapper();

        assertThat(mapper).isNotNull();
        assertThat(mapper.getMarshaller().marshall(new Friend("test"))).isNotNull();
    }
}
