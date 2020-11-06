package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RFC6901Tests {
    @Test
    void testRFC6901Compliance() throws IOException {
        JsonNode data = TestUtils.loadResourceAsJsonNode("/rfc6901/data.json");
        JsonNode testData = data.get("testData");

        ObjectNode emptyJson = TestUtils.DEFAULT_MAPPER.createObjectNode();
        JsonNode patch = JsonDiff.asJson(emptyJson, testData);
        JsonNode result = JsonPatch.apply(patch, emptyJson);
        assertEquals(testData, result);
    }
}
