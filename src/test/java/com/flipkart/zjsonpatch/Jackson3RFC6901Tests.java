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
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Jackson 3.x compatibility tests for RFC 6901 compliance.
 * Tests the same functionality as {@link RFC6901Tests} but using Jackson 3.x APIs.
 *
 * @author Mariusz Sondecki
 */
class Jackson3RFC6901Tests {

    @Test
    void testRFC6901Compliance() throws IOException {
        JsonNode data = Jackson3TestUtils.loadResourceAsJsonNode("/rfc6901/data.json");
        JsonNode testData = data.get("testData");

        ObjectNode emptyJson = Jackson3TestUtils.DEFAULT_MAPPER.createObjectNode();
        JsonNode patch = Jackson3JsonDiff.asJson(emptyJson, testData);
        JsonNode result = Jackson3JsonPatch.apply(patch, emptyJson);
        assertEquals(testData, result);
    }
}
