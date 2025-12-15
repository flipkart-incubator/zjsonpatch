package com.flipkart.zjsonpatch;

import com.flipkart.zjsonpatch.mapping.JacksonVersionBridge;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;

public class JsonPointerEvaluationException extends Exception {
    private final AbstractJsonPointer path;
    private final JsonNodeWrapper target;

    public JsonPointerEvaluationException(String message, AbstractJsonPointer path, JsonNodeWrapper target) {
        super(message);
        this.path = path;
        this.target = target;
    }

    public JsonPointerEvaluationException(String message, Throwable cause) {
        super(message, cause);
        this.path = null;
        this.target = null;
    }

    /**
     * Returns the JSON pointer path that caused the evaluation failure.
     * This method returns the appropriate pointer type based on context:
     * <ul>
     *   <li>{@link JsonPointer} when using Jackson 2.x APIs (JsonDiff, JsonPatch, etc.)</li>
     *   <li>{@link Jackson3JsonPointer} when using Jackson 3.x APIs (Jackson3JsonDiff, etc.)</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * try {
     *     pointer.evaluate(node);
     * } catch (JsonPointerEvaluationException e) {
     *     AbstractJsonPointer path = e.getPath();
     *     // Works with both Jackson 2.x and 3.x
     *     System.out.println("Failed at: " + path);
     * }
     * }</pre>
     *
     * <p><b>Breaking Change in 0.6.0:</b> This method now returns {@link AbstractJsonPointer}
     * instead of {@link JsonPointer}. Code that previously declared
     * {@code JsonPointer path = e.getPath();} must be updated to use
     * {@code AbstractJsonPointer path = e.getPath();}. This change enables
     * support for both Jackson 2.x and 3.x without requiring both versions
     * on the classpath.</p>
     *
     * @return the JSON pointer path where evaluation failed, or null
     */
    public AbstractJsonPointer getPath() {
        return path;
    }

    /**
     * Returns the target node where the JSON pointer evaluation failed.
     *
     * <p>This method returns the underlying native Jackson node type (Jackson 2.x or 3.x)
     * based on the node involved in the failed evaluation. In environments where both
     * Jackson 2 and 3 are present, callers must not assume a particular JsonNode type;
     * expecting the wrong type may result in {@link ClassCastException}.</p>
     *
     * @param <NODE> the expected Jackson JsonNode type
     * @return the target JsonNode in its original Jackson version, or null
     */
    public <NODE> NODE getTarget() {
        return target != null ? JacksonVersionBridge.unwrap(target) : null;
    }
}
