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

package org.jongo.marshall.jackson;

import com.mongodb.DBObject;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jongo.util.BsonUtil.bsonify;


public class JacksonEngineTest {

    JacksonEngine engine = new JacksonEngine(new Mapping.Builder().build());

    @Test(expected = MarshallingException.class)
    public void shouldFailWhenUnableToMarshall() throws Exception {

        engine.marshall(new ErrorObject());
    }

    @Test
    public void shouldFailWhenUnableToUnmarshall() throws Exception {

        try {
            engine.unmarshall(bsonify("{'error':'notADate'}"), ErrorObject.class);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
        }
    }

    @Test
    public void canMarshall() {

        BsonDocument doc = engine.marshall(new Fox("fantastic", "roux"));

        DBObject dbo = doc.toDBObject();
        assertThat(dbo.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(dbo.get("name")).isEqualTo("fantastic");
        assertThat(dbo.get("color")).isEqualTo("roux");
    }

    @Test
    public void canUnmarshallBson() throws IOException {

        BsonDocument document = bsonify("{'address': '22 rue des murlins'}");

        Friend friend = engine.unmarshall(document, Friend.class);

        assertThat(friend.getAddress()).isEqualTo("22 rue des murlins");
    }

}
