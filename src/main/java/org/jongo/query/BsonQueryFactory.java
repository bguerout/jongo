/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com>, Yves AMSELLEM <amsellem dot yves at gmail dot com>
 * and other contributors
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

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONCallback;
import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;
import org.bson.BSON;
import org.bson.BSONObject;
import org.jongo.bson.Bson;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;

import java.util.Collection;
import java.util.List;

public class BsonQueryFactory implements QueryFactory {

    private static final String DEFAULT_TOKEN = "#";
    private static final String MARSHALL_OPERATOR = "$marshall";
    private static final String PRECEDING_VALUE_PARAM = ": ,[\t\r\n";

    private final String token;
    private final Marshaller marshaller;
    private final ObjectSerializer jsonSerializer;

    private static class BsonQuery implements Query {
        private final DBObject dbo;

        public BsonQuery(DBObject dbo) {
            this.dbo = dbo;
        }

        public DBObject toDBObject() {
            return dbo;
        }
    }

    public BsonQueryFactory(Marshaller marshaller) {
        this(marshaller, DEFAULT_TOKEN);
    }

    public BsonQueryFactory(Marshaller marshaller, String token) {
        this.token = token;
        this.marshaller = marshaller;
        this.jsonSerializer = JSONSerializers.getStrict();
    }

    public Query createQuery(final String query, Object... parameters) {

        if (query == null) {
            return new BsonQuery((DBObject) JSON.parse(query));
        }
        if (parameters == null) {
            parameters = new Object[]{null};
        }

        // We have two different cases:
        //
        // - tokens as property names "{scores.#: 1}": they must be expanded before going
        //   through the JSON parser, and their toString() is inserted in the query
        //
        // - tokens as property values "{id: #}": they are resolved by the JSON parser and
        //   therefore marshalled as DBObjects (actually LazyDBObjects).

        StringBuilder sb = new StringBuilder();
        int paramIncrement = 0; // how many params must be skipped by the next value param
        int paramPos = 0;       // current position in the parameter list
        int start = 0;          // start of the current string segment
        int pos;                // position of the last token found
        while ((pos = query.indexOf(token, start)) != -1) {
            if (paramPos >= parameters.length) {
                throw new IllegalArgumentException("Not enough parameters passed to query: " + query);
            }

            // Insert chars before the token
            sb.append(query, start, pos);

            // Check if the character preceding the token is one that separates values.
            // Otherwise, it's a property name substitution
            boolean isValueParam = true;
            if (pos > 0) {
                char c = query.charAt(pos - 1);
                if (PRECEDING_VALUE_PARAM.indexOf(c) == -1) {
                    isValueParam = false;
                }
            }

            if (isValueParam) {
                // Will be resolved by the JSON parser below
                sb.append("{\"").append(MARSHALL_OPERATOR).append("\":").append(paramIncrement).append("}");
                paramIncrement = 0;
            } else {
                // Resolve it now
                sb.append(parameters[paramPos]);
                paramIncrement++;
            }

            paramPos++;
            start = pos + token.length();
        }

        // Add remaining chars
        sb.append(query, start, query.length());

        if (paramPos < parameters.length) {
            throw new IllegalArgumentException("Too many parameters passed to query: " + query);
        }


        final Object[] params = parameters;

        // Parse the query with a callback that will weave in marshalled parameters
        DBObject dbo;
        try {
            dbo = (DBObject) JSON.parse(sb.toString(), new JSONCallback() {

                int paramPos = 0;

                @Override
                public Object objectDone() {
                    String name = curName();
                    Object o = super.objectDone();

                    if (o instanceof BSONObject && !(o instanceof List<?>)) {
                        BSONObject dbo = (BSONObject) o;
                        Object marshallValue = dbo.get(MARSHALL_OPERATOR);
                        if (marshallValue != null) {
                            paramPos += ((Number) marshallValue).intValue();
                            if (paramPos >= params.length) {
                                throw new IllegalArgumentException("Not enough parameters passed to query: " + query);
                            }

                            o = marshallParameter(params[paramPos++], false);

                            // Replace value set by super.objectDone()
                            if (!isStackEmpty()) {
                                _put(name, o);
                            } else {
                                o = !BSON.hasDecodeHooks() ? o : BSON.applyDecodingHooks(o);
                                setRoot(o);
                            }
                        }
                    }

                    if (isStackEmpty()) {
                        // End of object
                    }

                    return o;
                }
            });

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse query: " + query, e);
        }

        return new BsonQuery(dbo);

    }

    private Object marshallParameter(Object parameter, boolean serializeBsonPrimitives) {
        try {
            if (parameter == null || Bson.isPrimitive(parameter)) {
                return serializeBsonPrimitives ? jsonSerializer.serialize(parameter) : parameter;
            }
            if (parameter instanceof Enum) {
                String name = ((Enum<?>) parameter).name();
                return serializeBsonPrimitives ? jsonSerializer.serialize(name) : name;
            }
            if (parameter instanceof Collection) {
                return marshallCollection((Collection<?>) parameter);
            }
            if (parameter instanceof Object[]) {
                return marshallArray((Object[]) parameter);
            }
            return marshallDocument(parameter);
        } catch (Exception e) {
            String message = String.format("Unable to marshall parameter: %s", parameter);
            throw new MarshallingException(message, e);
        }
    }

    private DBObject marshallArray(Object[] parameters) {
        BasicDBList list = new BasicDBList();
        for (int i = 0; i < parameters.length; i++) {
            list.add(marshallParameter(parameters[i], false));
        }
        return list;
    }

    private DBObject marshallCollection(Collection<?> parameters) {
        BasicDBList list = new BasicDBList();
        for (Object param : parameters) {
            list.add(marshallParameter(param, false));
        }
        return list;
    }

    private DBObject marshallDocument(Object parameter) {
        return marshaller.marshall(parameter).toDBObject();
    }
}
