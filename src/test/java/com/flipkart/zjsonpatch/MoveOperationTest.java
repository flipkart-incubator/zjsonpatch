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
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author ctranxuan (streamdata.io).
 */
public class MoveOperationTest extends AbstractTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Parameterized.Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("move");
    }

    @Test
    public void testMoveValueGeneratedHasNoValue() throws IOException {
        JsonNode jsonNode1 = MAPPER.readTree("{ \"foo\": { \"bar\": \"baz\", \"waldo\": \"fred\" }, \"qux\": { \"corge\": \"grault\" } }");
        JsonNode jsonNode2 = MAPPER.readTree("{ \"foo\": { \"bar\": \"baz\" }, \"qux\": { \"corge\": \"grault\", \"thud\": \"fred\" } }");
        JsonNode patch = MAPPER.readTree("[{\"op\":\"move\",\"from\":\"/foo/waldo\",\"path\":\"/qux/thud\"}]");

        JsonNode diff = JsonDiff.asJson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }

    @Test
    public void testMoveArrayGeneratedHasNoValue() throws IOException {
        JsonNode jsonNode1 = MAPPER.readTree("{ \"foo\": [ \"all\", \"grass\", \"cows\", \"eat\" ] }");
        JsonNode jsonNode2 = MAPPER.readTree("{ \"foo\": [ \"all\", \"cows\", \"eat\", \"grass\" ] }");
        JsonNode patch = MAPPER.readTree("[{\"op\":\"move\",\"from\":\"/foo/1\",\"path\":\"/foo/3\"}]");

        JsonNode diff = JsonDiff.asJson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }
}
