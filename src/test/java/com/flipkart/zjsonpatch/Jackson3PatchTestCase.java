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

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Jackson 3.x version of {@link PatchTestCase} for loading test data.
 * 
 * @author Mariusz Sondecki
 */
final class Jackson3PatchTestCase {

    private final boolean operation;
    private final JsonNode node;
    private final String sourceFile;

    private Jackson3PatchTestCase(boolean isOperation, JsonNode node, String sourceFile) {
        this.operation = isOperation;
        this.node = node;
        this.sourceFile = sourceFile;
    }

    boolean isOperation() {
        return operation;
    }

    JsonNode getNode() {
        return node;
    }

    String getSourceFile() {
        return sourceFile;
    }

    static Collection<Jackson3PatchTestCase> load(String fileName) throws IOException {
        String path = "/testdata/" + fileName + ".json";
        JsonNode tree = Jackson3TestUtils.loadResourceAsJsonNode(path);

        List<Jackson3PatchTestCase> result = new ArrayList<>();
        for (JsonNode node : tree.get("errors")) {
            if (isEnabled(node)) {
                result.add(new Jackson3PatchTestCase(false, node, path));
            }
        }
        for (JsonNode node : tree.get("ops")) {
            if (isEnabled(node)) {
                result.add(new Jackson3PatchTestCase(true, node, path));
            }
        }
        return result;
    }

    private static boolean isEnabled(JsonNode node) {
        JsonNode disabled = node.get("disabled");
        return (disabled == null || !disabled.booleanValue());
    }

    boolean isApplyInPlaceSupported() {
        JsonNode allowInPlace = node.get("allowInPlace");
        return (allowInPlace == null || allowInPlace.booleanValue());
    }
}
