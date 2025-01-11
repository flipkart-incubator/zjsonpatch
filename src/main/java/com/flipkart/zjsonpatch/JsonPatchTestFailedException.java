package com.flipkart.zjsonpatch;

public class JsonPatchTestFailedException extends JsonPatchApplicationException {
  public JsonPatchTestFailedException(String message, JsonPointer path) {
    super(message, Operation.TEST, path);
  }
}
