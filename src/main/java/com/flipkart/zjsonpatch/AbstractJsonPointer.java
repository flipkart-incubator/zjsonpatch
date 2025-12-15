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


import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public abstract sealed class AbstractJsonPointer permits JsonPointer, Jackson3JsonPointer {
    private final RefToken[] tokens;

    protected AbstractJsonPointer(RefToken[] tokens) {
        this.tokens = tokens;
    }

    /**
     * Constructs a new pointer from a list of reference tokens.
     *
     * @param tokens The list of reference tokens from which to construct the new pointer. This list is not modified.
     */
    protected AbstractJsonPointer(List<RefToken> tokens) {
        this.tokens = tokens.toArray(new RefToken[0]);
    }

    protected abstract AbstractJsonPointer createInstance(RefToken[] tokens);

    /**
     * Indicates whether or not this instance points to the root of a JSON document.
     * @return {@code true} if this pointer represents the root node, {@code false} otherwise.
     */
    public boolean isRoot() {
        return tokens.length == 0;
    }

    protected RefToken[] getTokens() {
        return tokens;
    }

    protected static List<RefToken> parseTokens(String path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        
        StringBuilder reftoken = null;
        List<RefToken> result = new ArrayList<RefToken>();

        for (int i = 0; i < path.length(); ++i) {
            char c = path.charAt(i);

            // Require leading slash
            if (i == 0) {
                if (c != '/') throw new IllegalArgumentException("Missing leading slash");
                reftoken = new StringBuilder();
                continue;
            }

            switch (c) {
                // Escape sequences
                case '~':
                    if (i + 1 >= path.length()) {
                        throw new IllegalArgumentException("Incomplete escape sequence '~' at end of path");
                    }
                    switch (path.charAt(++i)) {
                        case '0':
                        case '1':
                        case '2':
                            reftoken.append('~');
                            reftoken.append(path.charAt(i));
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid escape sequence ~" + path.charAt(i) + " at index " + i);
                    }
                    break;

                // New reftoken
                case '/':
                    result.add(RefToken.parse(reftoken.toString()));
                    reftoken.setLength(0);
                    break;

                default:
                    reftoken.append(c);
                    break;
            }
        }

        if (reftoken != null) {
            result.add(RefToken.parse(reftoken.toString()));
        }

        return result;
    }

    /**
     * Creates a new JSON pointer to the specified field of the object referenced by this instance.
     *
     * @param field The desired field name, or any valid JSON Pointer reference token
     * @return The new {@link AbstractJsonPointer} instance.
     */
    AbstractJsonPointer append(String field) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(field, null, null);
        return createInstance(newTokens);
    }

    /**
     * Creates a new JSON pointer to an indexed element of the array referenced by this instance.
     *
     * @param index The desired index, or {@link #LAST_INDEX} to point past the end of the array.
     * @return The new {@link AbstractJsonPointer} instance.
     */
    AbstractJsonPointer append(int index) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(Integer.toString(index), index, null);
        return createInstance(newTokens);
    }

    /** Returns the number of reference tokens comprising this instance. */
    int size() {
        return tokens.length;
    }

    /**
     * Returns a string representation of this instance
     *
     * @return
     *  An <a href="https://tools.ietf.org/html/rfc6901#section-5">RFC 6901 compliant</a> string
     *  representation of this JSON pointer.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RefToken token : tokens) {
            sb.append('/');
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Decomposes this JSON pointer into its reference tokens.
     *
     * @return A list of {@link RefToken}s. Modifications to this list do not affect this instance.
     */
    public List<RefToken> decompose() {
        return Arrays.asList(tokens.clone());
    }

    /**
     * Retrieves the reference token at the specified index.
     *
     * @param index The desired reference token index.
     * @return The specified instance of {@link RefToken}.
     * @throws IndexOutOfBoundsException The specified index is illegal.
     */
    public RefToken get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= tokens.length) throw new IndexOutOfBoundsException("Illegal index: " + index);
        return tokens[index];
    }

    /**
     * Retrieves the last reference token for this JSON pointer.
     *
     * @return The last {@link RefToken} comprising this instance.
     * @throws IllegalStateException Last cannot be called on root pointers.
     */
    public RefToken last() {
        if (isRoot()) throw new IllegalStateException("Root pointers contain no reference tokens");
        return tokens[tokens.length - 1];
    }

    private void error(int atToken, String message, JsonNodeWrapper document) throws JsonPointerEvaluationException {
        throw new JsonPointerEvaluationException(
                message,
                createInstance(Arrays.copyOf(tokens, atToken)),
                document);
    }

    JsonNodeWrapper evaluate(final JsonNodeWrapper document) throws JsonPointerEvaluationException {
        JsonNodeWrapper current = document;

        for (int idx = 0; idx < tokens.length; ++idx) {
            final RefToken token = tokens[idx];

            if (current.isArray()) {
                if (token.isArrayIndex()) {
                    if (token.getIndex() == LAST_INDEX || token.getIndex() >= current.size())
                        error(idx, "Array index " + token + " is out of bounds", document);
                    current = current.get(token.getIndex());
                } else if (token.isArrayKeyRef()) {
                    KeyRef keyRef = token.getKeyRef();
                    JsonNodeWrapper foundArrayNode = null;
                    for (int arrayIdx = 0; arrayIdx < current.size(); ++arrayIdx) {
                        JsonNodeWrapper arrayNode = current.get(arrayIdx);
                        if (matches(keyRef, arrayNode)) {
                            foundArrayNode = arrayNode;
                            break;
                        }
                    }
                    if (foundArrayNode == null) {
                        error(idx, "Array has no matching object for key reference " + token, document);
                    }
                    current = foundArrayNode;
                } else {
                    error(idx, "Can't reference field \"" + token.getField() + "\" on array", document);
                }
            }
            else if (current.isObject()) {
                if (!current.has(token.getField()))
                    error(idx,"Missing field \"" + token.getField() + "\"", document);
                current = current.get(token.getField());
            }
            else
                error(idx, "Can't reference past scalar value", document);
        }

        return current;
    }

    private boolean matches(KeyRef keyRef, JsonNodeWrapper arrayNode) {
        boolean matches = false;
        if (arrayNode.has(keyRef.key)) {
             JsonNodeWrapper valueNode = arrayNode.get(keyRef.key);
             if (valueNode.isTextual()) {
                 matches = Objects.equals(keyRef.value, valueNode.textValue());
             } else if (valueNode.isNumber() || valueNode.isBoolean()) {
                 matches = Objects.equals(keyRef.value, valueNode.toString());
             }
        }
        return matches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractJsonPointer that = (AbstractJsonPointer) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tokens);
    }


    /**
     * Represents an array index pointing past the end of the array.
     *
     * Such an index is represented by the JSON pointer reference token "{@code -}"; see
     * <a href="https://tools.ietf.org/html/rfc6901#section-4">RFC 6901 section 4</a> for
     * more details.
     */
    final static int LAST_INDEX = Integer.MIN_VALUE;
}
