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

package com.certusoft.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Chris Mueller on 01/07/2019.
 *
 * Extends JsonDiff to support more intelligent comparison based on type
 * -BigDecimal comparison with Epsilon for 'equality' tests
 */

public final class JsonDiffTyped extends JsonDiff {

    private JsonDiffTyped() {

    }

    // This value of precision is acceptable for our purposes but this epsilon should be configurable for general cases.
    private static final BigDecimal EPSILON = new BigDecimal(0.000001); // BigDecimal equality epsilon

    private static void generateDiffs(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        if (!source.equals(target)) {
            NodeType sourceType = NodeType.getNodeType(source);
            NodeType targetType = NodeType.getNodeType(target);
            if (sourceType == NodeType.ARRAY && targetType == NodeType.ARRAY) {
                compareArray(diffs, path, source, target);
            } else if (sourceType == NodeType.OBJECT && targetType == NodeType.OBJECT) {
                compareObjects(diffs, path, source, target);
            } else if (source.isBigDecimal() && target.isBigDecimal()) {
                if (source.decimalValue().subtract(target.decimalValue()).abs().compareTo(EPSILON) > 0) {
                    // If the difference between the BigDecimal values is > EPSILON count as a difference
                    diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
                }
            } else {
                diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
            }
        }

    }
}
