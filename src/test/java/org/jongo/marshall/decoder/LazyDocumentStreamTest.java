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

package org.jongo.marshall.decoder;

import org.jongo.marshall.DocumentStream;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.Unmarshaller;
import org.jongo.util.BSON;
import org.jongo.util.ErrorObject;
import org.junit.Test;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LazyDocumentStreamTest {

    @Test
    public void shouldFailWhenUnableToUnmarshall() throws Exception {

        Unmarshaller unmarshaller = mock(Unmarshaller.class);
        DocumentStream document = BSON.bsonify("{'error':'notADate'}");
        LazyDocumentStream dbObject = new LazyDocumentStream(document.getData(), 0, null, unmarshaller);
        when(unmarshaller.unmarshall(dbObject, ErrorObject.class)).thenThrow(new MarshallingException("error"));

        try {
            dbObject.as(ErrorObject.class);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
            assertThat(e.getMessage()).contains("{ \"error\" : \"notADate\"}");
            e.printStackTrace();
        }
    }
}
