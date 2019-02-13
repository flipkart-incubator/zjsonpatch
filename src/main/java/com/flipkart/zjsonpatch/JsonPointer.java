package com.flipkart.zjsonpatch;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JsonPointer {
    final RefToken[] tokens;

    final static JsonPointer ROOT = new JsonPointer(new RefToken[] {});

    private JsonPointer(RefToken[] tokens) {
        this.tokens = tokens;
    }

    public JsonPointer(List<RefToken> tokens) {
        this.tokens = tokens.toArray(new RefToken[0]);
    }

    private static Pattern JSON_POINTER_PATTERN = Pattern.compile("\\G/(.*?)(?=/|\\z)");

    public static JsonPointer parse(String path) {
        Matcher matcher = JSON_POINTER_PATTERN.matcher(path);
        List<RefToken> result = new ArrayList<RefToken>();
        while (matcher.find()) {
            result.add(RefToken.parse(matcher.group(1)));
        }
        if (result.isEmpty()) return ROOT;
        return new JsonPointer(result);
    }

    public boolean isRoot() {
        return tokens.length == 0;
    }

    JsonPointer at(String field) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(field);
        return new JsonPointer(newTokens);
    }

    JsonPointer at(int index) {
        return at(Integer.toString(index));
    }


    int size() {
        return tokens.length;
    }

    public String toString() {
        if (tokens.length == 0) return "/";
        StringBuilder sb = new StringBuilder();
        for (RefToken token : tokens) {
            sb.append('/');
            sb.append(token);
        }
        return sb.toString();
    }

    public List<RefToken> decompose() {
        return Arrays.asList(tokens.clone());
    }

    public RefToken get(int index) {
        if (index < 0 || index >= tokens.length) throw new IndexOutOfBoundsException("Illegal index: " + index);
        return tokens[index];
    }

    public RefToken last() {
        if (isRoot()) throw new IllegalStateException("Last is meaningless on root");
        return tokens[tokens.length - 1];
    }

    public JsonPointer getParent() {
        return isRoot() ? this : new JsonPointer(Arrays.copyOf(tokens, tokens.length - 1));
    }

    public JsonNode evaluate(final JsonNode document) throws JsonPointerEvaluationException {
        JsonNode current = document;

        for (RefToken token : tokens) {

            if (current.isArray() && token.isArrayIndex()) {
                if (token.getIndex() == LAST_INDEX || token.getIndex() >= current.size())
                    throw new JsonPointerEvaluationException("Can't address past array bounds", this, document);
                current = current.get(token.getIndex());
            }
            else if (current.isObject()) {
                if (!current.has(token.getField()))
                    throw new JsonPointerEvaluationException("Missing field \"" + token.getField() + "\"", this, document);
                current = current.get(token.getField());
            }
            else throw new JsonPointerEvaluationException("Can't reference past scalar value", this, document);
        }

        return current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonPointer that = (JsonPointer) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tokens);
    }

    static class RefToken {
        private String decodedToken;
        transient private Integer index = null;

        public RefToken(String decodedToken) {
            if (decodedToken == null) throw new IllegalArgumentException("Token can't be null");
            this.decodedToken = decodedToken;
        }

        private static final Pattern DECODED_TILDA_PATTERN = Pattern.compile("~0");
        private static final Pattern DECODED_SLASH_PATTERN = Pattern.compile("~1");

        private static String decodePath(Object object) {
            String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
            path = DECODED_SLASH_PATTERN.matcher(path).replaceAll("/");
            return DECODED_TILDA_PATTERN.matcher(path).replaceAll("~");
        }

        private static final Pattern ENCODED_TILDA_PATTERN = Pattern.compile("~");
        private static final Pattern ENCODED_SLASH_PATTERN = Pattern.compile("/");

        private static String encodePath(Object object) {
            String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
            path = ENCODED_TILDA_PATTERN.matcher(path).replaceAll("~0");
            return ENCODED_SLASH_PATTERN.matcher(path).replaceAll("~1");
        }

        private static final Pattern VALID_ARRAY_IND = Pattern.compile("-|0|(?:[1-9][0-9]*)");

        public static RefToken parse(String rawToken) {
            if (rawToken == null) throw new IllegalArgumentException("Token can't be null");
            return new RefToken(decodePath(rawToken));
        }

        public boolean isArrayIndex() {
            if (index != null) return true;
            Matcher matcher = VALID_ARRAY_IND.matcher(decodedToken);
            if (matcher.matches()) {
                index = matcher.group().equals("-") ? LAST_INDEX : Integer.parseInt(matcher.group());
                return true;
            }
            return false;
        }

        public int getIndex() {
            if (!isArrayIndex()) throw new IllegalArgumentException("Object operation on array target");
            return index;
        }

        public String getField() {
            return decodedToken;
        }

        @Override
        public String toString() {
            return encodePath(decodedToken);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RefToken refToken = (RefToken) o;

            return decodedToken.equals(refToken.decodedToken);
        }

        @Override
        public int hashCode() {
            return decodedToken.hashCode();
        }
    }

    final static int LAST_INDEX = Integer.MIN_VALUE;
}
