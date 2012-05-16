package org.jongo;

import org.jongo.marshall.Marshaller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterBinder {

    private static final String DEFAULT_TOKEN = "#";
    private final String token;
    private final Pattern pattern;
    private final Marshaller marshaller;

    public ParameterBinder(Marshaller marshaller) {
        this(marshaller, DEFAULT_TOKEN);
    }

    public ParameterBinder(Marshaller marshaller, String token) {
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
            String paramAsJson = marshaller.marshall(parameter);
            return query.replaceFirst(token, getMatcherWithEscapedDollar(paramAsJson));

        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to bind parameter: " + parameter + " into query: " + query, e);
        }
    }

    private void assertThatParamsCanBeBound(String template, Object[] parameters) {
        int nbTokens = countTokens(template);
        if (nbTokens != parameters.length) {
            throw new IllegalArgumentException("Tokens and parameters numbers mismatch " +
                    "[query: " + template + " / tokens:" + nbTokens + " / parameters:[" + parameters.length + "]");
        }
    }

    /**
     * http://veerasundar.com/blog/2010/01/java-lang-illegalargumentexception-illegal-group-reference-in-string-replaceall/
     */
    private String getMatcherWithEscapedDollar(String serialized) {
        return Matcher.quoteReplacement(serialized);
    }

    private int countTokens(String template) {
        return pattern.split(template, 0).length - 1;
    }
}
