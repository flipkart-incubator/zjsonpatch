package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

interface JsonPatchProcessor {
    void remove(List<String> path);
    void replace(List<String> path, JsonNode value);
    void add(List<String> path, JsonNode value);
    void move(List<String> fromPath, List<String> toPath);
}
