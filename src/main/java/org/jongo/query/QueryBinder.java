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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.MarshallingException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QueryBinder {

    private static final String DEFAULT_TOKEN = "#";
    private final String token;
    private final Pattern pattern;
    private final Marshaller marshaller;

    public QueryBinder(Marshaller marshaller) {
        this(marshaller, DEFAULT_TOKEN);
    }

    public QueryBinder(Marshaller marshaller, String token) {
        this.marshaller = marshaller;
        this.token = token;
        this.pattern = Pattern.compile(token);
    }

    public String bind(String template, Object... parameters) {
        assertThatParamsCanBeBound(template, parameters);
        return generateQueryFromTemplate(template, parameters);
    }

    private String generateQueryFromTemplate(String template, Object[] parameters) {
        String query = template;
        int paramIndex = 0;
        while (query.contains(token)) {
            Object parameter = parameters[paramIndex++];
            query = bindParamIntoQuery(query, parameter);
        }
        return query;
    }

    private String bindParamIntoQuery(String query, Object parameter) {

        try {
            String paramAsJson = marshallParameter(parameter);
            return query.replaceFirst(token, getMatcherWithEscapedDollar(paramAsJson));

        } catch (RuntimeException e) {
            return handleInvalidBinding(query, parameter, e);
        }
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


    private String handleInvalidBinding(String query, Object parameter, RuntimeException e) {
        String message = String.format("Unable to bind parameter: %s into query: %s", parameter, query);
        throw new IllegalArgumentException(message, e);
    }

    private void assertThatParamsCanBeBound(String template, Object[] parameters) {
        int nbTokens = countTokens(template);
        if (nbTokens != parameters.length) {
            String message = String.format("Unable to bind parameters into query: %s. Tokens and parameters numbers mismatch " +
                    "[tokens: %s / parameters:%s]", template, nbTokens, parameters.length);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * http://veerasundar.com/blog/2010/01/java-lang-illegalargumentexception-illegal-group-reference-in-string-replaceall/
     */
    private String getMatcherWithEscapedDollar(String serialized) {
        return Matcher.quoteReplacement(serialized);
    }

    private int countTokens(String template) {
        int count = 0;
        Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
