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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jackson 3.x compatibility tests for JsonDiff functionality.
 * Tests the same functionality as {@link JsonDiffTest} but using Jackson 3.x APIs.
 * 
 * @author Mariusz Sondecki
 */
class Jackson3JsonDiffTest {
    private static final ObjectMapper objectMapper = JsonMapper.builder().build();
    private static ArrayNode jsonNode;

    @BeforeAll
    static void beforeClass() throws IOException {
        String path = "/testdata/sample.json";
        try (InputStream resourceAsStream = Jackson3JsonDiffTest.class.getResourceAsStream(path)) {
            assertNotNull(resourceAsStream, "Resource " + path + " not found on classpath");
            String testData = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            jsonNode = (ArrayNode) objectMapper.readTree(testData);
        }
    }

    @Test
    void testSampleJsonDiff() {
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode first = jsonNode.get(i).get("first");
            JsonNode second = jsonNode.get(i).get("second");
            JsonNode actualPatch = Jackson3JsonDiff.asJson(first, second);
            JsonNode secondPrime = Jackson3JsonPatch.apply(actualPatch, first);
            assertEquals(second, secondPrime, "JSON Patch not symmetrical [index=" + i + ", first=" + first + "]");
        }
    }

    @Test
    void testGeneratedJsonDiff() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            JsonNode first = Jackson3TestDataGenerator.generate(random.nextInt(10));
            JsonNode second = Jackson3TestDataGenerator.generate(random.nextInt(10));
            JsonNode actualPatch = Jackson3JsonDiff.asJson(first, second);
            JsonNode secondPrime = Jackson3JsonPatch.apply(actualPatch, first);
            assertEquals(second, secondPrime);
        }
    }

    @Test
    void testRenderedRemoveOperationOmitsValueByDefault() {
        ObjectNode source = objectMapper.createObjectNode();
        ObjectNode target = objectMapper.createObjectNode();
        source.put("field", "value");

        JsonNode diff = Jackson3JsonDiff.asJson(source, target);

        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).get("op").asString());
        assertEquals("/field", diff.get(0).get("path").asString());
        assertNull(diff.get(0).get("value"));
    }

    @Test
    void testRenderedRemoveOperationRetainsValueIfOmitDiffFlagNotSet() {
        ObjectNode source = objectMapper.createObjectNode();
        ObjectNode target = objectMapper.createObjectNode();
        source.put("field", "value");

        EnumSet<DiffFlags> flags = DiffFlags.defaults().clone();
        assertTrue(flags.remove(DiffFlags.OMIT_VALUE_ON_REMOVE), "Expected OMIT_VALUE_ON_REMOVE by default");
        JsonNode diff = Jackson3JsonDiff.asJson(source, target, flags);

        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).get("op").asString());
        assertEquals("/field", diff.get(0).get("path").asString());
        assertEquals("value", diff.get(0).get("value").asString());
    }

    @Test
    void testRenderedOperationsExceptMoveAndCopy() throws Exception {
        JsonNode source = objectMapper.readTree("{\"age\": 10}");
        JsonNode target = objectMapper.readTree("{\"height\": 10}");

        EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone(); //only have ADD, REMOVE, REPLACE, Don't normalize operations into MOVE & COPY

        JsonNode diff = Jackson3JsonDiff.asJson(source, target, flags);

        for (JsonNode d : diff) {
            assertNotEquals(Operation.MOVE.rfcName(), d.get("op").asString());
            assertNotEquals(Operation.COPY.rfcName(), d.get("op").asString());
        }

        JsonNode targetPrime = Jackson3JsonPatch.apply(diff, source);
        assertEquals(target, targetPrime);
    }

    @Test
    void testPath() {
        JsonNode source = objectMapper.readTree("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world\"}]}}");
        JsonNode patch = objectMapper.readTree("[{\"op\":\"copy\",\"from\":\"/profiles/def/0\", \"path\":\"/profiles/def/0\"},{\"op\":\"replace\",\"path\":\"/profiles/def/0/hello\",\"value\":\"world2\"}]");

        JsonNode target = Jackson3JsonPatch.apply(patch, source);
        JsonNode expected = objectMapper.readTree("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world2\"},{\"hello\":\"world\"}]}}");
        assertEquals(expected, target);
    }

    @Test
    void testJsonDiffReturnsEmptyNodeExceptionWhenBothSourceAndTargetNodeIsNull() {
        JsonNode diff = Jackson3JsonDiff.asJson(null, null);
        assertEquals(0, diff.size());
    }

    @Test
    void testJsonDiffShowsDiffWhenSourceNodeIsNull() {
        String target = "{ \"K1\": {\"K2\": \"V1\"} }";
        JsonNode diff = Jackson3JsonDiff.asJson(null, objectMapper.reader().readTree(target));
        assertEquals(1, diff.size());

        assertEquals(Operation.ADD.rfcName(), diff.get(0).get("op").asString());
        assertEquals(JsonPointer.ROOT.toString(), diff.get(0).get("path").asString());
        assertEquals("V1", diff.get(0).get("value").get("K1").get("K2").asString());
    }

    @Test
    void testJsonDiffShowsDiffWhenTargetNodeIsNull() {
        String source = "{ \"K1\": {\"K2\": \"V1\"} }";
        JsonNode diff = Jackson3JsonDiff.asJson(objectMapper.reader().readTree(source), null);
        assertEquals(1, diff.size());

        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).get("op").asString());
        assertEquals(JsonPointer.ROOT.toString(), diff.get(0).get("path").asString());
    }

    @Test
    void testJsonDiffShowsDiffWhenTargetNodeIsNullWithFlags() {
        String source = "{ \"K1\": \"V1\" }";
        JsonNode sourceNode = objectMapper.reader().readTree(source);
        JsonNode diff = Jackson3JsonDiff.asJson(sourceNode, null, EnumSet.of(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE));

        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).get("op").asString());
        assertEquals(JsonPointer.ROOT.toString(), diff.get(0).get("path").asString());
        assertEquals("V1", diff.get(0).get("value").get("K1").asString());
    }
}
