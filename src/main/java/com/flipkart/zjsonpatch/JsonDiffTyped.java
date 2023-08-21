/*
 * Copyright (c) 2023 Certusoft, Inc. All Rights Reserved.
 *
 * This SOURCE CODE FILE, which has been provided by Certusoft as part
 * of a Certusoft product for use ONLY by licensed users of the product,
 * includes CONFIDENTIAL and PROPRIETARY information of Certusoft.
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS
 * OF THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH
 * THE PRODUCT.
 */

package com.flipkart.zjsonpatch;

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

    // This value of precision is acceptable for our purposes but this epsilon should be configurable for general cases.
    private static final BigDecimal EPSILON = new BigDecimal(0.000001); // BigDecimal equality epsilon
    private BigDecimal epsilon = EPSILON;

    public JsonDiffTyped() {

    }

    public JsonDiffTyped(BigDecimal epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    protected boolean generateDiffs(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        if (!source.equals(target)) {
            NodeType sourceType = NodeType.getNodeType(source);
            NodeType targetType = NodeType.getNodeType(target);
            if (sourceType == NodeType.ARRAY && targetType == NodeType.ARRAY) {
                compareArray(diffs, path, source, target);
            } else if (sourceType == NodeType.OBJECT && targetType == NodeType.OBJECT) {
                compareObjects(diffs, path, source, target);
            } else if (source.isBigDecimal() && target.isBigDecimal()) {
                if (source.decimalValue().subtract(target.decimalValue()).abs().compareTo(epsilon) > 0) {
                    // If the difference between the BigDecimal values is > EPSILON count as a difference
                    diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
                    return true;
                }
            } else {
                diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
                return true;
            }
        }
        return false;
    }
}
