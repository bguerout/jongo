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

package org.jongo.bson;

import com.mongodb.BasicDBObject;
import com.mongodb.DBEncoder;
import com.mongodb.LazyDBObject;
import org.bson.io.BasicOutputBuffer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BsonDBEncoderTest {

    @Test
    public void shouldPipeLazyDbObject() throws Exception {

        DBEncoder encoder = BsonDBEncoder.FACTORY.create();
        BasicOutputBuffer buffer = new BasicOutputBuffer();

        encoder.writeObject(buffer, new LazyDBObject(new byte[]{5, 0, 0, 0, 0}, null));

        assertThat(buffer.toByteArray()).isEqualTo(new byte[]{5, 0, 0, 0, 0});

    }

    @Test
    public void shouldEncodeDBObject() throws Exception {
        DBEncoder encoder = BsonDBEncoder.FACTORY.create();
        BasicOutputBuffer buffer = new BasicOutputBuffer();

        encoder.writeObject(buffer, new BasicDBObject());

        assertThat(buffer.size()).isGreaterThan(0);
    }
}
