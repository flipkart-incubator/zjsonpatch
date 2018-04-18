package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * A normalizing function, used to calculated and transform a series of
 * {@link Diff}'s in to another type of {@link Diff}.
 */
interface OperationNormalizer {
    void normalize(JsonNode source, JsonNode target, List<Diff> diffs);
}
