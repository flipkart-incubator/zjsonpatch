package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/** A JSON patch processor that does nothing, intended for testing and validation. */
public class NoopProcessor implements JsonPatchProcessor {
    static NoopProcessor INSTANCE;
    static {
        INSTANCE = new NoopProcessor();
    }

    @Override public void remove(List<String> path) {}
    @Override public void replace(List<String> path, JsonNode value) {}
    @Override public void add(List<String> path, JsonNode value) {}
    @Override public void move(List<String> fromPath, List<String> toPath) {}
}
