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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import java.util.regex.Matcher;

public class QueryTemplate {

    private static final char DEFAULT_TOKEN = '#';
    private final String token;

    public QueryTemplate() {
        this("" + DEFAULT_TOKEN);
    }

    public QueryTemplate(String token) {
        this.token = token;
    }


    public DBObject parameterize(String query, Object... parameters) {

        parameters = convertNullVargsToANullParameter(parameters);
        checkIfAllParametersCanBeInserted(query, parameters);

        int paramIndex = 0;
        while (query.contains(token)) {
            Object parameter = parameters[paramIndex++];
            query = parameterizeFirstToken(query, parameter);
        }


        return ((DBObject) JSON.parse(query));
    }

    private void checkIfAllParametersCanBeInserted(String query, Object[] parameters) {
        int nbTokens = countTokens(query);
        if (nbTokens > parameters.length) {
            throw new IllegalArgumentException("Query has more anchors " + nbTokens + " than parameters" + parameters.length);
        }
    }

    private Object[] convertNullVargsToANullParameter(Object[] parameters) {
        if (parameters == null) {
            parameters = new Object[]{null};
        }
        return parameters;
    }

    private String parameterizeFirstToken(String query, Object param) {
        String paramAsJson = JSON.serialize(param);
        query = query.replaceFirst("#", getMatcherWithEscapedDollar(paramAsJson));
        return query;
    }


    /**
     * http://veerasundar.com/blog/2010/01/java-lang-illegalargumentexception-illegal-group-reference-in-string-replaceall/
     */
    private String getMatcherWithEscapedDollar(String serialized) {
        return Matcher.quoteReplacement(serialized);
    }

    private int countTokens(String json) {
        return json.split(token).length - 1;
    }
}
