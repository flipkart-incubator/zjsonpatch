package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPointerEvaluationException extends Exception {
    public JsonPointerEvaluationException(String message, JsonPointer path, JsonNode target) {
        super(message);
    }
}
