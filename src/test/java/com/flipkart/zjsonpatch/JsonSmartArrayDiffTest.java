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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author ctranxuan (streamdata.io).
 */
public class JsonSmartArrayDiffTest {

    private static final int START_INDEX = 0;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ArrayNode jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/smart-array-move.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = (ArrayNode) objectMapper.readTree(testData);
    }

    @Test
    public void testSampleJsonDiff() {
        for (int i = START_INDEX; i < jsonNode.size(); i++) {
            JsonNode first = jsonNode.get(i).get("first");
            JsonNode second = jsonNode.get(i).get("second");
            JsonNode patch = jsonNode.get(i).get("patch");
            JsonNodeEqualsFunction jsonNodeEqualFunction = new JsonNodeEqualsFunction() {
                @Override
                public boolean equals(JsonNode jsonNode1, JsonNode jsonNode2) {
                    if (jsonNode1 == null || jsonNode2 == null) {
                        return false;
                    }
                    if (jsonNode1.has("id") && jsonNode2.has("id")) {
                        return jsonNode1.get("id").asInt() == jsonNode2.get("id").asInt();
                    }
                    return jsonNode1.equals(jsonNode2);
                }

            };
            JsonNode actualPatch = JsonDiff.asJson(first, second, DiffFlags.defaults(), jsonNodeEqualFunction);
            Assert.assertEquals("JSON Patch not equal [index=" + i + ", first=" + first + "]", patch, actualPatch);
            JsonNode secondPrime = JsonPatch.apply(actualPatch, first);
            Assert.assertEquals("JSON Patch applies not symmetrical [index=" + i + ", first=" + first + "]", second,
                    secondPrime);
        }
    }
}
