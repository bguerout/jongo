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

package org.jongo.marshall.jackson;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;
import org.jongo.marshall.QueryMarshaller;
import org.jongo.query.BsonPrimitives;

public class JacksonQueryMarshaller implements QueryMarshaller {

    private final Marshaller marshaller;

    public JacksonQueryMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public String marshallParameter(Object parameter) {
        if (BsonPrimitives.contains(parameter.getClass()))
            return JSON.serialize(parameter);
        if (parameter instanceof Enum) {
            return JSON.serialize(((Enum) parameter).name());
        } else
            return marshall(parameter).toString();
    }

    private DBObject marshall(Object parameter) {
        try {
            return marshaller.marshall(parameter);
        } catch (Exception e) {
            String message = String.format("Unable to marshall parameter: %s", parameter);
            throw new MarshallingException(message, e);
        }
    }

    public DBObject marshallQuery(String query) {
        try {
            return (DBObject) JSON.parse(query);
        } catch (Exception e) {
            throw new IllegalArgumentException(query + " cannot be parsed", e);
        }
    }

}
