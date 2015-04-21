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

package org.jongo.query;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONCallback;
import org.bson.BSON;
import org.bson.BSONObject;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BsonQueryFactory implements QueryFactory {

    private static final String DEFAULT_TOKEN = "#";
    private static final String MARSHALL_OPERATOR = "$marshall";

    private final String token;
    private final Marshaller marshaller;
    private boolean removeKeyIfNull;

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
        this(marshaller, token, false);
    }

    public BsonQueryFactory(Marshaller marshaller, boolean removeKeyIfNull) {
        this(marshaller, DEFAULT_TOKEN, removeKeyIfNull);
    }

    public BsonQueryFactory(Marshaller marshaller, String token, boolean removeKeyIfNull) {
        this.token = token;
        this.marshaller = marshaller;
        this.removeKeyIfNull = removeKeyIfNull;
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
            if (isValueToken(query, pos)) {
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
                public void gotNull(String name) {
                    if (!removeKeyIfNull) {
                        super.gotNull(name);
                    }
                }

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

                            o = marshallParameter(params[paramPos++]);

                            // Replace value set by super.objectDone()
                            if (removeKeyIfNull && o == null) {
                                cur().removeField(name);
                            } else {
                                if (!isStackEmpty()) {
                                    _put(name, o);
                                } else {
                                    o = !BSON.hasDecodeHooks() ? o : BSON.applyDecodingHooks(o);
                                    setRoot(o);
                                }
                            }
                        } else {
                            if (name != null && o instanceof BasicDBObject) {
                                BasicDBObject basic = (BasicDBObject) o;
                                if (basic.size() == 0 && removeKeyIfNull) {
                                    cur().removeField(name);
                                    return null;
                                }
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

    private boolean isValueToken(String query, int tokenIndex) {
        for (int pos = tokenIndex; pos >= 0; pos--) {
            char c = query.charAt(pos);
            if (c == ':') {
                return true;
            } else if (c == '{' || c == '.') {
                return false;
            } else if (c == ',') {
                return !isPropertyName(query, pos - 1);
            }
        }
        return true;
    }

    private boolean isPropertyName(String query, int tokenIndex) {
        for (int pos = tokenIndex; pos >= 0; pos--) {
            char c = query.charAt(pos);
            if (c == '[') {
                return false;
            } else if (c == '{') {
                return true;
            }
        }
        return false;
    }

    private Object marshallParameter(Object parameter) {
        try {
            if (parameter == null || Bson.isPrimitive(parameter)) {
                return parameter;
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
            list.add(marshallParameter(parameters[i]));
        }
        return list;
    }

    private DBObject marshallCollection(Collection<?> parameters) {
        BasicDBList list = new BasicDBList();
        for (Object param : parameters) {
            list.add(marshallParameter(param));
        }
        return list;
    }

    private Object marshallDocument(Object parameter) {

        if (parameter instanceof Enum) {
            return marshallParameterAsPrimitive(parameter);
        } else {
            BsonDocument document = marshaller.marshall(parameter);

            if (hasBeenSerializedAsPrimitive(document)) {
                return marshallParameterAsPrimitive(parameter);
            } else {
                return document.toDBObject();
            }
        }
    }

    private boolean hasBeenSerializedAsPrimitive(BsonDocument document) {
        return document.toByteArray()[0] == 0;
    }

    /**
     * The object may have been serialized to a primitive type with a
     * custom serializer, so try again after wrapping as an object property.
     * We do this trick only as a falllback since it causes Jackson to consider the parameter
     * as "Object" and thus ignore any annotations that may exist on its actual class.
     */
    private Object marshallParameterAsPrimitive(Object parameter) {
        Map<String, Object> primitiveWrapper = Collections.singletonMap("wrapped", parameter);
        BsonDocument document = marshaller.marshall(primitiveWrapper);
        return document.toDBObject().get("wrapped");
    }
}
