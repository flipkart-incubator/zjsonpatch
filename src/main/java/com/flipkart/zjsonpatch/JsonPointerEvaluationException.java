package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPointerEvaluationException extends Exception {
    private final JsonPointer path;
    private final JsonPointer fullPath;
    private final JsonNode target;

    public JsonPointerEvaluationException(String message, JsonPointer path, JsonPointer fullPath, JsonNode target) {
        super(message);
        this.path = path;
        this.fullPath = fullPath;
        this.target = target;
    }

    public JsonPointer getPath() {
        return path;
    }

    public JsonNode getTarget() {
        return target;
    }

    public JsonPointer getFullPath() {
        return fullPath;
    }
}
