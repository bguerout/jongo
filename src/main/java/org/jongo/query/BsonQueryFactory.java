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

package org.jongo.query;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bson.BsonDocumentWrapper;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;

import java.util.*;

public class BsonQueryFactory implements QueryFactory {

    private static final String DEFAULT_TOKEN = "#";

    /**
     * The marshall operator will be replacing the token during query parsing as following:
     * {"firstname":#} -> {"firstname":{MARSHALL_OPERATOR: 0}}
     * 0 being the index of the parameter to be inserted in place of that placeholder.
     * Previously $marshall but upgrading to mongo driver 4 the new parser does not allow $ prefixed strings
     * if they're not mongo operators.
     * With a UUID prefixed string there should be no risk of collision.
     */
    private static final String MARSHALL_OPERATOR = "8a6e4178-8fba-4d22-af43-840512e3a999-marshall";

    private final String token;
    private final Marshaller marshaller;

    private static class BsonQuery implements Query {
        private final DBObject dbo;

        public BsonQuery(DBObject dbo) {
            this.dbo = dbo;
        }

        public DBObject toDBObject() {
            return dbo;
        }

        public org.bson.BsonDocument toBsonDocument() {
            return BsonDocumentWrapper.asBsonDocument(dbo, MongoClient.getDefaultCodecRegistry());
        }
    }

    public BsonQueryFactory(Marshaller marshaller) {
        this(marshaller, DEFAULT_TOKEN);
    }

    public BsonQueryFactory(Marshaller marshaller, String token) {
        this.token = token;
        this.marshaller = marshaller;
    }

    public Query createQuery(final String query, Object... parameters) {

        if (query == null) {
            return new BsonQuery(null);
        }
        if (parameters == null) {
            parameters = new Object[]{null};
        }

        String quotedQuery = addRequiredQuotesAndParameters(query, parameters);

        final Object[] params = parameters;

        DBObject dbo;
        try {
            if (quotedQuery.charAt(0) == '[') {
                // little hack to handle first class arrays as BasicDBObject cannot parse them
                // also we could do this for simple objects but it would not handle properly queries like
                // "{'a':1}, {'b':1}" as tested in MongoCollectionTest.canCreateGeospacialIndex()
                dbo = (DBObject) BasicDBObject.parse("{'query':" + quotedQuery + "}").get("query");
            } else {
                dbo = BasicDBObject.parse(quotedQuery);
            }

            if (params.length != 0) {
                dbo = (DBObject) replaceParams(dbo, params);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse query: " + query, e);
        }

        return new BsonQuery(dbo);
    }

    private String addRequiredQuotesAndParameters(String query, Object[] parameters) {
        StringBuilder sb = new StringBuilder(query.length());

        int position = 0;
        int paramIndex = 0;
        Stack<Context> stack = new Stack<>(Context.NONE);
        Context ctx;
        String token = "";
        String previousToken = "";
        char currentStringStartingQuote = ' ';
        for (char c : query.toCharArray()) {
            ctx = stack.peek();
            if (ctx == Context.STRING) {
                token += c;
                if (c == currentStringStartingQuote) {
                    stack.pop();
                }
            } else if (c == '\'' || c == '"') {
                stack.push(Context.STRING);
                currentStringStartingQuote = c;
                token += c;
            } else if ((token + c).lastIndexOf(this.token) == token.length() - this.token.length() + 1) {
                if (paramIndex >= parameters.length) {
                    throw new IllegalArgumentException("Not enough parameters passed to query: " + query);
                }
                if ("$oid".equals(previousToken) ||
                        !isValueToken(query, position)) {
                    token = "\"" + token.trim() + parameters[paramIndex] + "\"";
                } else {
                    sb.append("{\"")
                            .append(MARSHALL_OPERATOR)
                            .append("\":")
                            .append(paramIndex).append("}");
                }
                paramIndex++;
            } else if (c == '{') {
                stack.push(Context.OBJECT);
                sb.append("{");
            } else if (c == '[') {
                stack.push(Context.ARRAY);
                sb.append("[");
            } else if (c == '}') {
                ctx = stack.pop();
                if (ctx != Context.OBJECT) {
                    throw new IllegalArgumentException("Invalid token at position: " + position);
                }

                if (!token.isEmpty()) {
                    sb.append(token);
                }

                previousToken = token;
                token = "";
                sb.append("}");
            } else if (c == ']') {
                ctx = stack.pop();
                if (ctx != Context.ARRAY) {
                    throw new IllegalArgumentException("Invalid token at position: " + position);
                }

                if (!token.isEmpty()) {
                    sb.append(token);
                }

                previousToken = token;
                token = "";
                sb.append("]");
            } else if (c == ':') {
                String key = token.trim();
                if (key.isEmpty() || key.equals("\"\"") || key.equals("''")) {
                    throw new IllegalArgumentException("Invalid token at position: " + position);
                }

                sb.append(isQuoted(key) ? key : quote(key));
                sb.append(":");
                previousToken = token;
                token = "";
            } else if (c == ',') {
                sb.append(token);
                sb.append(",");
                previousToken = token;
                token = "";
            } else {
                token += c;
            }

            position++;
        }

        if (paramIndex < parameters.length) {
            throw new IllegalArgumentException("Too many parameters passed to query: " + query);
        }

        return sb.toString().trim();
    }

    public enum Context {
        NONE,
        OBJECT,
        ARRAY,
        STRING;
    }

    private static boolean isQuoted(String token) {
        char start = token.charAt(0);
        char end = token.charAt(token.length() - 1);
        if (start == '\'' && end == '\'') {
            return true;
        }

        if (start == '"' && end == '"') {
            return true;
        }

        return false;
    }

    private static String quote(String token) {
        return "\"" + token + "\"";
    }

    private static class Stack<T> {
        private final LinkedList<T> stack;
        private final T noValue;

        private Stack(T noValue) {
            this.stack = new LinkedList<>();
            this.noValue = noValue;
        }

        public T peek() {
            if (stack.isEmpty()) {
                return noValue;
            }

            return stack.peekLast();
        }

        public T pop() {
            if (stack.isEmpty()) {
                return noValue;
            }

            return this.stack.removeLast();
        }

        public void push(T value) {
            this.stack.addLast(value);
        }
    }

    private Object replaceParams(DBObject dbo, Object[] params) {
        Set<String> keySet = dbo.keySet();
        if (keySet.size() == 1 && keySet.contains(MARSHALL_OPERATOR)) {
            return marshallParameter(params[(int) dbo.get(MARSHALL_OPERATOR)]);
        }

        keySet.forEach(key -> {
            Object value = dbo.get(key);
            if (value instanceof DBObject) {
                Object newValue = replaceParams((DBObject) value, params);
                if (newValue != value) {
                    dbo.put(key, newValue);
                }
            }
        });

        return dbo;

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
        for (final Object parameter : parameters) {
            list.add(marshallParameter(parameter));
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
        byte[] bytes = document.toByteArray();
        if (bytes.length > 4) {
            return bytes.length != document.getSize();
        }
        return true;
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
