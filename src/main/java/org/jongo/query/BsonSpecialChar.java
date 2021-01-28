package org.jongo.query;

import java.util.HashMap;
import java.util.Map;

enum BsonSpecialChar {

    LEFT_CURLY_BRACE('{') {
        @Override
        String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position) {
            ctxStack.push(Context.OBJECT);
            result.append(getSpecialChar());
            return "";
        }
    },
    LEFT_SQUARE_BRACKET('[') {
        @Override
        String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position) {
            ctxStack.push(Context.ARRAY);
            result.append(getSpecialChar());
            return "";
        }
    },
    RIGHT_CURLY_BRACE('}') {
        @Override
        String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position) {
            Context ctx = ctxStack.pop();
            if (ctx != Context.OBJECT) {
                throw new IllegalArgumentException("Invalid currentToken at position: " + position);
            }

            if (isNotEmpty(currentToken)) {
                result.append(currentToken);
            }

            return appendNextCharAndPopCurrentToken(result, currentToken, getSpecialChar());
        }
    },
    RIGHT_SQUARE_BRACKET(']') {
        @Override
        String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position) {
            Context ctx = ctxStack.pop();
            if (ctx != Context.ARRAY) {
                throw new IllegalArgumentException("Invalid currentToken at position: " + position);
            }
            if (isNotEmpty(currentToken)) {
                result.append(currentToken);
            }
            return appendNextCharAndPopCurrentToken(result, currentToken, getSpecialChar());
        }
    },
    COLON(':') {
        @Override
        String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position) {
            String key = currentToken.toString().trim();
            if (key.isEmpty() || key.equals("\"\"") || key.equals("''")) {
                throw new IllegalArgumentException("Invalid currentToken at position: " + position);
            }
            result.append(isQuoted(key) ? key : quote(key));
            return appendNextCharAndPopCurrentToken(result, currentToken, getSpecialChar());
        }
    },
    COMMA(',') {
        @Override
        String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position) {
            result.append(currentToken);
            return appendNextCharAndPopCurrentToken(result, currentToken, getSpecialChar());
        }
    };

    private static final Map<Character, BsonSpecialChar> BSON_SPECIAL_CHARS_MAP;

    static {
        BSON_SPECIAL_CHARS_MAP = new HashMap<>(BsonSpecialChar.values().length);
        for (BsonSpecialChar c : BsonSpecialChar.values()) {
            BSON_SPECIAL_CHARS_MAP.put(c.getSpecialChar(), c);
        }
    }

    private final char specialChar;

    BsonSpecialChar(char specialChar) {
        this.specialChar = specialChar;
    }

    char getSpecialChar() {
        return specialChar;
    }

    abstract String applySpecificBehaviour(StringBuilder result, StringBuilder currentToken, Stack<Context> ctxStack, int position);

    private static boolean isNotEmpty(StringBuilder sb) {
        return sb.length() > 0;
    }

    private static String appendNextCharAndPopCurrentToken(StringBuilder result, StringBuilder currentToken, char nextChar) {
        String previousToken = currentToken.toString();
        currentToken.setLength(0);
        result.append(nextChar);
        return previousToken;
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

    static boolean itIsABsonSpecialChar(char c) {
        return BSON_SPECIAL_CHARS_MAP.containsKey(c);
    }

    static BsonSpecialChar specialChar(char c) {
        return BSON_SPECIAL_CHARS_MAP.get(c);
    }

}
