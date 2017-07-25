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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public abstract class AbstractTest {

    @Parameter
    public PatchTestCase p;

    @Test
    public void test() throws Exception {
        if (p.isOperation()) {
            testOperation();
        } else {
            testError();
        }
    }

    private void testOperation() throws Exception {
        JsonNode node = p.getNode();

        JsonNode first = node.get("node");
        JsonNode second = node.get("expected");
        JsonNode patch = node.get("op");
        String message = node.has("message") ? node.get("message").toString() : "";

        JsonNode secondPrime = JsonPatch.apply(patch, first);

        assertThat(message, secondPrime, equalTo(second));
    }

    private Class<?> exceptionType(String type) throws ClassNotFoundException {
        return Class.forName(type.contains(".") ? type : "com.flipkart.zjsonpatch." + type);
    }

    private String errorMessage(String header) throws JsonProcessingException {
        String testCase = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(p.getNode());
        return header + "\nFull test case (in file " + p.getSourceFile() + "):\n" + testCase;
    }

    private void testError() throws JsonProcessingException, ClassNotFoundException {
        JsonNode node = p.getNode();
        JsonNode first = node.get("node");
        JsonNode patch = node.get("op");
        JsonNode message = node.get("message");
        Class<?> type =
                node.has("type") ? exceptionType(node.get("type").textValue()) : JsonPatchApplicationException.class;

        try {
            JsonPatch.apply(patch, first);

            fail(errorMessage("Failure expected: " + message));
        } catch (Exception e) {

            assertThat(errorMessage("Operation failed but with wrong exception type"), e, instanceOf(type));

            if (message != null) {
                assertThat(
                        errorMessage("Operation failed but with wrong message"),
                        e.getMessage(),
                        containsString(message.textValue()));    // equalTo would be better, but fail existing tests
            }
        }
    }
}
