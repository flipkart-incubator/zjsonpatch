/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.mapping.JacksonVersionBridge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JacksonVersionBridge to verify correct version detection and node wrapping.
 *
 * @author Mariusz Sondecki
 */
class JacksonVersionBridgeTest {

    @Test
    void testVersionBridgeHandlesSubclassedNodesInJsonOperations() {
        CustomObjectNode source = new CustomObjectNode(
            com.fasterxml.jackson.databind.node.JsonNodeFactory.instance);
        source.put("name", "test");

        ObjectNode target = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        target.put("name", "updated");
        target.put("new", "field");

        JsonNode patch = JsonDiff.asJson(source, target);

        assertNotNull(patch);
        assertEquals(2, patch.size(), "Should generate correct patch with subclassed source");

        JsonNode result = JsonPatch.apply(patch, source);
        assertEquals(target, result, "Should correctly apply patch to subclassed node");
    }

    @Test
    void testNonJacksonNodes() {
        String notAJsonNode = "This is not a Jackson node";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JacksonVersionBridge.wrap(notAJsonNode));

        assertTrue(exception.getMessage().contains("Unknown Jackson node type"));
        assertTrue(exception.getMessage().contains("java.lang.String"));
        assertTrue(exception.getMessage().contains("Expected node extending"));
    }

    private static class CustomObjectNode extends ObjectNode {
        public CustomObjectNode(com.fasterxml.jackson.databind.node.JsonNodeFactory nc) {
            super(nc);
        }
    }

}