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

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jackson 3.x version of {@link AbstractTest} for parameterized patch testing.
 * 
 * @author Mariusz Sondecki
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class Jackson3AbstractTest {

    protected boolean matchOnErrors() {
        return true;
    }

    @ParameterizedTest
    @MethodSource("data")
    void testApply(Jackson3PatchTestCase p) throws Exception {
        if (p.isOperation()) {
            testOperation(p, false);
        } else {
            testError(p, false);
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void testApplyInPlace(Jackson3PatchTestCase p) throws Exception {
        if (p.isOperation() && p.isApplyInPlaceSupported()) {
            testOperation(p, true);
        } else {
            testError(p, true);
        }
    }

    private void testOperation(Jackson3PatchTestCase p, boolean inPlace) {
        JsonNode node = p.getNode();

        JsonNode doc = node.get("node");
        JsonNode expected = node.get("expected");
        JsonNode patch = node.get("op");
        String message = node.has("message") ? node.get("message").toString() : "";

        JsonNode result;
        if (inPlace) {
            result = doc.deepCopy();
            Jackson3JsonPatch.applyInPlace(patch, result);
        } else {
            result = Jackson3JsonPatch.apply(patch, doc);
        }
        String failMessage = "The following test failed: \n" +
                "message: " + message + '\n' +
                "at: " + p.getSourceFile();
        assertEquals(expected, result, failMessage);
    }

    private Class<?> exceptionType(String type) throws ClassNotFoundException {
        return Class.forName(type.contains(".") ? type : "com.flipkart.zjsonpatch." + type);
    }

    private String errorMessage(String header, Exception e, Jackson3PatchTestCase p) {
        StringBuilder res =
                new StringBuilder()
                        .append(header)
                        .append("\nFull test case (in file ")
                        .append(p.getSourceFile())
                        .append("):\n")
                        .append(Jackson3TestUtils.DEFAULT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(p.getNode()));
        if (e != null) {
            res.append("\nFull error: ");
            e.printStackTrace(new PrintWriter(new StringBuilderWriter(res)));
        }
        return res.toString();
    }

    private void testError(Jackson3PatchTestCase p, boolean inPlace) throws Exception {
        JsonNode node = p.getNode();
        JsonNode first = node.get("node");
        JsonNode patch = node.get("op");
        JsonNode message = node.get("message");
        Class<?> type =
                node.has("type") ? exceptionType(node.get("type").asString()) : JsonPatchApplicationException.class;

        try {
            if (inPlace) {
                JsonNode target = first.deepCopy();
                Jackson3JsonPatch.applyInPlace(patch, target);
            } else {
                Jackson3JsonPatch.apply(patch, first);
            }
            fail("Failure expected: " + message + "\nAt: " + p.getSourceFile());
        } catch (Exception e) {
            if (matchOnErrors()) {
                assertTrue(type.isInstance(e), 
                        errorMessage("Operation failed but with wrong exception type", e, p));
                if (message != null) {
                    assertTrue(e.toString().contains(message.asString()),
                            errorMessage("Operation failed but with wrong message", e, p));
                }
            }
        }
    }
}
