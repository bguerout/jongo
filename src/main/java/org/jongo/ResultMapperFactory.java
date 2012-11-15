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

package org.jongo;

import com.mongodb.DBObject;
import org.jongo.bson.BsonDocument;
import org.jongo.bson.BsonDocumentFactory;
import org.jongo.marshall.Unmarshaller;

class ResultMapperFactory {


    public static <T> ResultMapper<T> newMapper(final Class<T> clazz, final Unmarshaller unmarshaller) {
        return new DefaultResultMapper<T>(unmarshaller, clazz);
    }

    private static class DefaultResultMapper<T> implements ResultMapper<T> {

        private final Unmarshaller unmarshaller;
        private final Class<T> clazz;

        public DefaultResultMapper(Unmarshaller unmarshaller, Class<T> clazz) {
            this.unmarshaller = unmarshaller;
            this.clazz = clazz;
        }

        public T map(DBObject result) {
            BsonDocument bsonDocument = BsonDocumentFactory.fromDBObject(result);
            return unmarshaller.unmarshall(bsonDocument, clazz);
        }
    }

    private ResultMapperFactory() {
    }
}
