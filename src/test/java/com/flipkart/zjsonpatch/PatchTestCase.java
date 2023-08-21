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
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PatchTestCase {

    private final boolean operation;
    private final JsonNode node;
    private final String sourceFile;

    private PatchTestCase(boolean isOperation, JsonNode node, String sourceFile) {
        this.operation = isOperation;
        this.node = node;
        this.sourceFile = sourceFile;
    }

    public boolean isOperation() {
        return operation;
    }

    public JsonNode getNode() {
        return node;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Collection<PatchTestCase> load(String fileName) throws IOException {
        String path = "/testdata/" + fileName + ".json";
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        JsonNode tree = MAPPER.readTree(testData);

        List<PatchTestCase> result = new ArrayList<PatchTestCase>();
        for (JsonNode node : tree.get("errors")) {
            if (isEnabled(node)) {
                result.add(new PatchTestCase(false, node, path));
            }
        }
        for (JsonNode node : tree.get("ops")) {
            if (isEnabled(node)) {
                result.add(new PatchTestCase(true, node, path));
            }
        }
        return result;
    }

    private static boolean isEnabled(JsonNode node) {
        JsonNode disabled = node.get("disabled");
        return (disabled == null || !disabled.booleanValue());
    }
}
