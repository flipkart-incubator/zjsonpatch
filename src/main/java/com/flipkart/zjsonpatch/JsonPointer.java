package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class JsonPointer {
    final RefToken[] tokens;

    final static JsonPointer ROOT = new JsonPointer(new RefToken[] {});

    private JsonPointer(RefToken[] tokens) {
        this.tokens = tokens;
    }

    public JsonPointer(List<RefToken> tokens) {
        this.tokens = tokens.toArray(new RefToken[0]);
    }

    JsonPointer atArrayIndex(int index) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(index);
        return new JsonPointer(newTokens);
    }

    JsonPointer atObjectField(String field) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(field);
        return new JsonPointer(newTokens);
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


    enum RefTokenKind {
        ARRAY_INDIRECTION,
        OBJECT_INDIRECTION
    }

    static class RefToken {
        private RefTokenKind kind;
        private int index = LAST_INDEX;
        private String field = null;

        public RefToken(int index) {
            this.kind = RefTokenKind.ARRAY_INDIRECTION;
            this.index = index;
        }

        public RefToken(String field) {
            this.kind = RefTokenKind.OBJECT_INDIRECTION;
            this.field = field;
        }

        RefTokenKind getKind() {
            return this.kind;
        }

        public int getIndex() {
            if (this.kind != RefTokenKind.ARRAY_INDIRECTION)
                throw new IllegalStateException("Not an array index");
            return index;
        }

        public String getField() {
            if (this.kind != RefTokenKind.OBJECT_INDIRECTION)
                throw new IllegalStateException("Not an object field name");
            return field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RefToken refToken = (RefToken) o;

            if (index != refToken.index) return false;
            if (kind != refToken.kind) return false;
            return field != null ? field.equals(refToken.field) : refToken.field == null;
        }

        @Override
        public int hashCode() {
            int result = kind.hashCode();
            result = 31 * result + index;
            result = 31 * result + (field != null ? field.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            switch (kind) {
                case ARRAY_INDIRECTION:
                    return index == LAST_INDEX ? "-" : Integer.toString(index);
                case OBJECT_INDIRECTION:
                    return field;
                default:
                    throw new IllegalArgumentException("Invalid kind \"" + kind.toString() + "\"");    // Safety net
            }
        }
    }

    final static int LAST_INDEX = Integer.MIN_VALUE;
}
