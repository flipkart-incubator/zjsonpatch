package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** A JSON patch processor that does nothing, intended for testing and validation. */
public class NoopProcessor implements JsonPatchProcessor {
    static NoopProcessor INSTANCE;
    static {
        INSTANCE = new NoopProcessor();
    }

    @Override
    public JsonNode add(List<String> path, JsonNode value) {
        return null;
    }

    @Override
    public JsonNode replace(List<String> path, JsonNode value) {
        return null;
    }

    @Override
    public JsonNode remove(List<String> path) {
        return null;
    }

    @Override
    public JsonNode move(List<String> fromPath, List<String> toPath) {
        return null;
    }

    @Override
    public JsonNode copy(List<String> fromPath, List<String> toPath) {
        return null;
    }
}
