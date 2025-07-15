package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

/** Custom equality function for JsonNode objects */
public interface JsonNodeEqualsFunction {
    JsonNodeEqualsFunction REF_IDENTITY = (jsonNode1, jsonNode2) -> jsonNode1.equals(jsonNode2);

    boolean equals(JsonNode jsonNode1, JsonNode jsonNode2);
}
