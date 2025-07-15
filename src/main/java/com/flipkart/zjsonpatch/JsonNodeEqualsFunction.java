package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Custom equality function for JsonNode objects.
 * Allows clients to define custom equality semantics for JSON node comparison.
 */
@FunctionalInterface
public interface JsonNodeEqualsFunction {
    JsonNodeEqualsFunction REF_IDENTITY = (jsonNode1, jsonNode2) -> jsonNode1.equals(jsonNode2);
    /**
     * Compares two JsonNode objects for equality based on custom logic.
     * @param jsonNode1 the first JsonNode to compare
     * @param jsonNode2 the second JsonNode to compare
     * @return true if the nodes are considered equal, false otherwise
     */
    boolean equals(JsonNode jsonNode1, JsonNode jsonNode2);
}
