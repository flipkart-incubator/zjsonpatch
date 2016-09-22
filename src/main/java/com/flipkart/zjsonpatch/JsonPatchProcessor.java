package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

interface JsonPatchProcessor {
    JsonNode remove(List<String> path);
    JsonNode replace(List<String> path, JsonNode value);
    JsonNode add(List<String> path, JsonNode value);
    JsonNode move(List<String> fromPath, List<String> toPath);
    JsonNode copy(List<String> fromPath, List<String> toPath);
}
