package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

enum NodeType {
    /**
     * Array nodes
     */
    ARRAY("array"),
    /**
     * Boolean nodes
     */
    BOOLEAN("boolean"),
    /**
     * Integer nodes
     */
    INTEGER("integer"),
    /**
     * Number nodes (ie, decimal numbers)
     */
    NULL("null"),
    /**
     * Object nodes
     */
    NUMBER("number"),
    /**
     * Null nodes
     */
    OBJECT("object"),
    /**
     * String nodes
     */
    STRING("string");

    /**
     * The name for this type, as encountered in a JSON schema
     */
    private final String name;

    private static final Map<JsonToken, NodeType> TOKEN_MAP
            = new EnumMap<JsonToken, NodeType>(JsonToken.class);

    static {
        TOKEN_MAP.put(JsonToken.START_ARRAY, ARRAY);
        TOKEN_MAP.put(JsonToken.VALUE_TRUE, BOOLEAN);
        TOKEN_MAP.put(JsonToken.VALUE_FALSE, BOOLEAN);
        TOKEN_MAP.put(JsonToken.VALUE_NUMBER_INT, INTEGER);
        TOKEN_MAP.put(JsonToken.VALUE_NUMBER_FLOAT, NUMBER);
        TOKEN_MAP.put(JsonToken.VALUE_NULL, NULL);
        TOKEN_MAP.put(JsonToken.START_OBJECT, OBJECT);
        TOKEN_MAP.put(JsonToken.VALUE_STRING, STRING);

    }

    NodeType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NodeType getNodeType(final JsonNode node) {
        final JsonToken token = node.asToken();
        final NodeType ret = TOKEN_MAP.get(token);
        Objects.requireNonNull(ret, () -> "unhandled token type " + token);
        return ret;
    }
}
