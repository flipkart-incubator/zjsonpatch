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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * User: holograph
 * Date: 03/08/16
 */
public class ApiTest {

    @Test
    public void applyInPlaceMutatesSource() throws Exception {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        ObjectNode source = newObjectNode();
        ObjectNode beforeApplication = source.deepCopy();
        JsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyDoesNotMutateSource() throws Exception {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        ObjectNode source = newObjectNode();
        JsonPatch.applyInPlace(patch, source);
        assertThat(source.findValue("b").asText(), is("b-value"));
    }

    @Test
    public void applyDoesNotMutateSource2() throws Exception {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        ObjectNode source = newObjectNode();
        ObjectNode beforeApplication = source.deepCopy();
        JsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyInPlaceMutatesSourceWithCompatibilityFlags() throws Exception {
        JsonNode patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\" }]");
        ObjectNode source = newObjectNode();
        JsonPatch.applyInPlace(patch, source, EnumSet.of(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
        assertTrue(source.findValue("b").isNull());
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingNonArrayPatchShouldThrowAnException() throws IOException {
        JsonNode invalid = objectMapper.readTree("{\"not\": \"a patch\"}");
        JsonNode to = readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAnInvalidArrayShouldThrowAnException() throws IOException {
        JsonNode invalid = readTree("[1, 2, 3, 4, 5]");
        JsonNode to = readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
        JsonNode invalid = readTree("[{\"op\": \"what\"}]");
        JsonNode to = readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingNonArrayPatchShouldThrowAnException() throws IOException {
        JsonNode invalid = readTree("{\"not\": \"a patch\"}");
        JsonPatch.validate(invalid);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingAnInvalidArrayShouldThrowAnException() throws IOException {
        JsonNode invalid = readTree("[1, 2, 3, 4, 5]");
        JsonPatch.validate(invalid);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
        JsonNode invalid = readTree("[{\"op\": \"what\"}]");
        JsonPatch.validate(invalid);
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static JsonNode readTree(String jsonString) throws IOException {
        return objectMapper.readTree(jsonString);
    }

    private ObjectNode newObjectNode() {
        return objectMapper.createObjectNode();
    }
}

