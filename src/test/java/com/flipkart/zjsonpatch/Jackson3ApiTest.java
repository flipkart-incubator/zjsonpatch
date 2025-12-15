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

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jackson 3.x compatibility tests for core API functionality.
 * Tests the same functionality as {@link ApiTest} but using Jackson 3.x APIs.
 *
 * @author Mariusz Sondecki
 */
class Jackson3ApiTest {

    private static final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void applyDoesNotMutateSource() {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        ObjectNode source = newObjectNode();
        ObjectNode beforeApplication = source.deepCopy();
        Jackson3JsonPatch.apply(patch, source);
        assertEquals(beforeApplication, source);
    }

    @Test
    void applyInPlaceMutatesSource() {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        ObjectNode source = newObjectNode();
        Jackson3JsonPatch.applyInPlace(patch, source);
        assertEquals("b-value", source.findValue("b").asString());
    }

    @Test
    void applyInPlaceMutatesSourceWithCompatibilityFlags() {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\" }]");
        ObjectNode source = newObjectNode();
        Jackson3JsonPatch.applyInPlace(patch, source, EnumSet.of(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
        assertTrue(source.findValue("b").isNull());
    }

    @Test
    void applyingNonArrayPatchShouldThrowAnException() {
        JsonNode invalid = objectMapper.readTree("{\"not\": \"a patch\"}");
        JsonNode to = readTree("{\"a\":1}");
        assertThrows(InvalidJsonPatchException.class, () -> Jackson3JsonPatch.apply(invalid, to));
    }

    @Test
    void applyingAnInvalidArrayShouldThrowAnException() {
        JsonNode invalid = readTree("[1, 2, 3, 4, 5]");
        JsonNode to = readTree("{\"a\":1}");
        assertThrows(InvalidJsonPatchException.class, () -> Jackson3JsonPatch.apply(invalid, to));
    }

    @Test
    void applyingAPatchWithAnInvalidOperationShouldThrowAnException() {
        JsonNode invalid = readTree("[{\"op\": \"what\"}]");
        JsonNode to = readTree("{\"a\":1}");
        assertThrows(InvalidJsonPatchException.class, () -> Jackson3JsonPatch.apply(invalid, to));
    }

    @Test
    void validatingNonArrayPatchShouldThrowAnException() {
        JsonNode invalid = readTree("{\"not\": \"a patch\"}");
        assertThrows(InvalidJsonPatchException.class, () -> Jackson3JsonPatch.validate(invalid));
    }

    @Test
    void validatingAnInvalidArrayShouldThrowAnException() {
        JsonNode invalid = readTree("[1, 2, 3, 4, 5]");
        assertThrows(InvalidJsonPatchException.class, () -> Jackson3JsonPatch.validate(invalid));
    }

    @Test
    void validatingAPatchWithAnInvalidOperationShouldThrowAnException() {
        JsonNode invalid = readTree("[{\"op\": \"what\"}]");
        assertThrows(InvalidJsonPatchException.class, () -> Jackson3JsonPatch.validate(invalid));
    }

    private static JsonNode readTree(String jsonString) {
        return objectMapper.readTree(jsonString);
    }

    private ObjectNode newObjectNode() {
        return objectMapper.createObjectNode();
    }
}
