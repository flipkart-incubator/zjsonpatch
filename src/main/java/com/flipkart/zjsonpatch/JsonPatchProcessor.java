package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

interface JsonPatchProcessor {
    JsonNode add(List<String> path, JsonNode value);
    JsonNode test(List<String> path, JsonNode value);
    JsonNode replace(List<String> path, JsonNode value);
    JsonNode remove(List<String> path);
    JsonNode move(List<String> fromPath, List<String> toPath);
    JsonNode copy(List<String> fromPath, List<String> toPath);
}
