/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.zjsonpatch;


import com.flipkart.zjsonpatch.mapping.JacksonVersionBridge;
import com.flipkart.zjsonpatch.mapping.JacksonVersionException;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;

/**
 * Implements RFC 6901 (JSON Pointer)
 *
 * <p>For full details, please refer to <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
 *
 * <p>Generally, a JSON Pointer is a string representation of a path into a JSON document.
 * This class implements the RFC as closely as possible, and offers several helpers and
 * utility methods on top of it:
 *
 * <pre>
 *      // Parse, build or render a JSON pointer
 *      String path = "/a/0/b/1";
 *      Jackson3JsonPointer ptr1 = Jackson3JsonPointer.{@link #parse}(path);
 *      Jackson3JsonPointer ptr2 = Jackson3JsonPointer.{@link #ROOT}.append("a").append(0).append("b").append(1);
 *      assert(ptr1.equals(ptr2));
 *      assert(path.equals(ptr1.toString()));
 *      assert(path.equals(ptr2.toString()));
 *
 *      // Evaluate a JSON pointer against a live document
 *      ObjectMapper om = new ObjectMapper();
 *      JsonNode doc = om.readTree("{\"foo\":[\"bar\", \"baz\"]}");
 *      JsonNode baz = Jackson3JsonPointer.parse("/foo/1").{@link #evaluate(JsonNode) evaluate}(doc);
 *      assert(baz.stringValue().equals("baz"));
 * </pre>
 *
 * <p>Instances of {@link Jackson3JsonPointer} and its constituent {@link RefToken}s are <b>immutable</b>.
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public final class Jackson3JsonPointer extends AbstractJsonPointer {

    /** A JSON pointer representing the root node of a JSON document */
    public final static Jackson3JsonPointer ROOT = new Jackson3JsonPointer(new RefToken[] {});

    private Jackson3JsonPointer(RefToken[] tokens) {
        super(tokens);
    }

    /**
     * Constructs a new pointer from a list of reference tokens.
     *
     * @param tokens The list of reference tokens from which to construct the new pointer. This list is not modified.
     */
    public Jackson3JsonPointer(List<RefToken> tokens) {
        super(tokens);
    }

    /**
     * Parses a valid string representation of a JSON Pointer.
     *
     * @param path The string representation to be parsed.
     * @return An instance of {@link Jackson3JsonPointer} conforming to the specified string representation.
     * @throws IllegalArgumentException The specified JSON Pointer is invalid.
     */
    public static Jackson3JsonPointer parse(String path) throws IllegalArgumentException {
        List<RefToken> tokens = parseTokens(path);
        return tokens.isEmpty() ? ROOT : new Jackson3JsonPointer(tokens);
    }

    /**
     * Creates a JSON pointer to the parent of the node represented by this instance.
     *
     * The parent of the {@link #ROOT root} pointer is the root pointer itself.
     *
     * @return A {@link Jackson3JsonPointer} to the parent node.
     */
    public Jackson3JsonPointer getParent() {
        return isRoot() ? this : new Jackson3JsonPointer(Arrays.copyOf(getTokens(), getTokens().length - 1));
    }

    /**
     * Takes a target document and resolves the node represented by this instance.
     *
     * The evaluation semantics are described in
     * <a href="https://tools.ietf.org/html/rfc6901#section-4">RFC 6901 section 4</a>.
     *
     * @param document The target document against which to evaluate the JSON pointer.
     * @return The {@link JsonNode} resolved by evaluating this JSON pointer.
     * @throws JsonPointerEvaluationException The pointer could not be evaluated.
     */
    public JsonNode evaluate(final JsonNode document) throws JsonPointerEvaluationException {
        try {
            JsonNodeWrapper wrappedDocument = JacksonVersionBridge.wrap(document);
            JsonNodeWrapper result = evaluate(wrappedDocument);
            return JacksonVersionBridge.unwrap(result);
        } catch (JsonPointerEvaluationException e) {
            throw e;
        } catch (IllegalArgumentException | JacksonVersionException e) {
            throw new JsonPointerEvaluationException("Failed to evaluate pointer: " + e.getMessage(), e);
        }
    }

    @Override
    protected Jackson3JsonPointer createInstance(RefToken[] tokens) {
        return new Jackson3JsonPointer(tokens);
    }

}
