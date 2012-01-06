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

package com.jongo.spikes;

import com.jongo.jackson.JsonProcessor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import java.util.regex.Matcher;

public class ParameterizedQuery implements Query {

    private static final char DEFAULT_TOKEN = '#';
    private final String token;
    private final JsonProcessor processor;
    private final String query;
    private Object[] parameters;

    public ParameterizedQuery(JsonProcessor processor, String query, Object[] parameters) {
        this.query = query;
        this.parameters = parameters;
        this.processor = processor;
        this.token = "" + DEFAULT_TOKEN;
        checkIfQueryCanBeParameterized();
    }

    @Override
    public DBObject toDBObject() {
        return processor.toDBObject(generateParameterizedQuery());
    }

    private String generateParameterizedQuery() {
        String parameterizedQuery = query;
        int paramIndex = 0;
        while (parameterizedQuery.contains(token)) {
            String paramAsJson = JSON.serialize(parameters[(paramIndex++)]);
            parameterizedQuery = parameterizedQuery.replaceFirst("#", getMatcherWithEscapedDollar(paramAsJson));
        }
        return parameterizedQuery;
    }

    private void checkIfQueryCanBeParameterized() {
        int nbTokens = countTokens();
        if (nbTokens > parameters.length) {
            throw new IllegalArgumentException("Query has more anchors " + nbTokens + " than parameters" + parameters.length);
        }
    }

    /**
     * http://veerasundar.com/blog/2010/01/java-lang-illegalargumentexception-illegal-group-reference-in-string-replaceall/
     */
    private String getMatcherWithEscapedDollar(String serialized) {
        return Matcher.quoteReplacement(serialized);
    }

    private int countTokens() {
        return query.split(token).length - 1;
    }
}
