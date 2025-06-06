package com.flipkart.zjsonpatch;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements RFC 6901 (JSON Pointer)
 *
 * <p>For full details, please refer to <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
 *
 * <p></p>Generally, a JSON Pointer is a string representation of a path into a JSON document.
 * This class implements the RFC as closely as possible, and offers several helpers and
 * utility methods on top of it:
 *
 * <pre>
 *      // Parse, build or render a JSON pointer
 *      String path = "/a/0/b/1";
 *      JsonPointer ptr1 = JsonPointer.{@link #parse}(path);
 *      JsonPointer ptr2 = JsonPointer.{@link #ROOT}.append("a").append(0).append("b").append(1);
 *      assert(ptr1.equals(ptr2));
 *      assert(path.equals(ptr1.toString()));
 *      assert(path.equals(ptr2.toString()));
 *
 *      // Evaluate a JSON pointer against a live document
 *      ObjectMapper om = new ObjectMapper();
 *      JsonNode doc = om.readTree("{\"foo\":[\"bar\", \"baz\"]}");
 *      JsonNode baz = JsonPointer.parse("/foo/1").{@link #evaluate(JsonNode) evaluate}(doc);
 *      assert(baz.textValue().equals("baz"));
 * </pre>
 *
 * <p>Instances of {@link JsonPointer} and its constituent {@link RefToken}s are <b>immutable</b>.
 *
 * @since 0.4.8
 */
public class JsonPointer {
    private final RefToken[] tokens;

    /** A JSON pointer representing the root node of a JSON document */
    public final static JsonPointer ROOT = new JsonPointer(new RefToken[] {});

    private JsonPointer(RefToken[] tokens) {
        this.tokens = tokens;
    }

    /**
     * Constructs a new pointer from a list of reference tokens.
     *
     * @param tokens The list of reference tokens from which to construct the new pointer. This list is not modified.
     */
    public JsonPointer(List<RefToken> tokens) {
        this.tokens = tokens.toArray(new RefToken[0]);
    }

    /**
     * Parses a valid string representation of a JSON Pointer.
     *
     * @param path The string representation to be parsed.
     * @return An instance of {@link JsonPointer} conforming to the specified string representation.
     * @throws IllegalArgumentException The specified JSON Pointer is invalid.
     */
    public static JsonPointer parse(String path) throws IllegalArgumentException {
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

        if (reftoken == null)
            return ROOT;

        result.add(RefToken.parse(reftoken.toString()));
        return new JsonPointer(result);
    }

    /**
     * Indicates whether or not this instance points to the root of a JSON document.
     * @return {@code true} if this pointer represents the root node, {@code false} otherwise.
     */
    public boolean isRoot() {
        return tokens.length == 0;
    }

    /**
     * Creates a new JSON pointer to the specified field of the object referenced by this instance.
     *
     * @param field The desired field name, or any valid JSON Pointer reference token
     * @return The new {@link JsonPointer} instance.
     */
    JsonPointer append(String field) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(field, null, null);
        return new JsonPointer(newTokens);
    }

    /**
     * Creates a new JSON pointer to an indexed element of the array referenced by this instance.
     *
     * @param index The desired index, or {@link #LAST_INDEX} to point past the end of the array.
     * @return The new {@link JsonPointer} instance.
     */
    JsonPointer append(int index) {
        RefToken[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
        newTokens[tokens.length] = new RefToken(Integer.toString(index), index, null);
        return new JsonPointer(newTokens);
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
     * @throws IllegalStateException Last cannot be called on {@link #ROOT root} pointers.
     */
    public RefToken last() {
        if (isRoot()) throw new IllegalStateException("Root pointers contain no reference tokens");
        return tokens[tokens.length - 1];
    }

    /**
     * Creates a JSON pointer to the parent of the node represented by this instance.
     *
     * The parent of the {@link #ROOT root} pointer is the root pointer itself.
     *
     * @return A {@link JsonPointer} to the parent node.
     */
    public JsonPointer getParent() {
        return isRoot() ? this : new JsonPointer(Arrays.copyOf(tokens, tokens.length - 1));
    }

    private void error(int atToken, String message, JsonNode document) throws JsonPointerEvaluationException {
        throw new JsonPointerEvaluationException(
                message,
                new JsonPointer(Arrays.copyOf(tokens, atToken)),
                document);
    }

    /**
     * Takes a target document and resolves the node represented by this instance.
     *
     * The evaluation semantics are described in
     * <a href="https://tools.ietf.org/html/rfc6901#section-4">RFC 6901 sectino 4</a>.
     *
     * @param document The target document against which to evaluate the JSON pointer.
     * @return The {@link JsonNode} resolved by evaluating this JSON pointer.
     * @throws JsonPointerEvaluationException The pointer could not be evaluated.
     */
    public JsonNode evaluate(final JsonNode document) throws JsonPointerEvaluationException {
        JsonNode current = document;

        for (int idx = 0; idx < tokens.length; ++idx) {
            final RefToken token = tokens[idx];

            if (current.isArray()) {
                if (token.isArrayIndex()) {
                    if (token.getIndex() == LAST_INDEX || token.getIndex() >= current.size())
                        error(idx, "Array index " + token + " is out of bounds", document);
                    current = current.get(token.getIndex());
                } else if (token.isArrayKeyRef()) {
                    KeyRef keyRef = token.getKeyRef();
                    JsonNode foundArrayNode = null;
                    for (int arrayIdx = 0; arrayIdx < current.size(); ++arrayIdx) {
                        JsonNode arrayNode = current.get(arrayIdx);
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

    private boolean matches(KeyRef keyRef, JsonNode arrayNode) {
        boolean matches = false;
        if (arrayNode.has(keyRef.key)) {
             JsonNode valueNode = arrayNode.get(keyRef.key);
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

        JsonPointer that = (JsonPointer) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tokens);
    }

    /** Represents a single JSON Pointer reference token. */
    static class RefToken {
        private final String decodedToken;
        private final Integer index;
        private final KeyRef keyRef;

        private RefToken(String decodedToken, Integer arrayIndex, KeyRef arrayKeyRef) {
            if (decodedToken == null) throw new IllegalArgumentException("Token can't be null");
            this.decodedToken = decodedToken;
            this.index = arrayIndex;
            this.keyRef = arrayKeyRef;
        }

        private static final Pattern DECODED_TILDA_PATTERN = Pattern.compile("~0");
        private static final Pattern DECODED_SLASH_PATTERN = Pattern.compile("~1");
        private static final Pattern DECODED_EQUALS_PATTERN = Pattern.compile("~2");

        private static String decodePath(Object object) {
            String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
            path = DECODED_SLASH_PATTERN.matcher(path).replaceAll("/");
            path = DECODED_TILDA_PATTERN.matcher(path).replaceAll("~");
            return DECODED_EQUALS_PATTERN.matcher(path).replaceAll("=");
        }

        private static final Pattern ENCODED_TILDA_PATTERN = Pattern.compile("~");
        private static final Pattern ENCODED_SLASH_PATTERN = Pattern.compile("/");
        private static final Pattern ENCODED_EQUALS_PATTERN = Pattern.compile("=");

        private static String encodePath(Object object) {
            String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
            path = ENCODED_TILDA_PATTERN.matcher(path).replaceAll("~0");
            path = ENCODED_SLASH_PATTERN.matcher(path).replaceAll("~1");
            return ENCODED_EQUALS_PATTERN.matcher(path).replaceAll("~2");
        }

        private static final Pattern VALID_ARRAY_IND = Pattern.compile("-|0|(?:[1-9][0-9]*)");

        private static final Pattern VALID_ARRAY_KEY_REF = Pattern.compile("([^=]+)=([^=]+)");

        public static RefToken parse(String rawToken) {
            if (rawToken == null) throw new IllegalArgumentException("Token can't be null");

            Integer index = null;
            Matcher indexMatcher = VALID_ARRAY_IND.matcher(rawToken);
            if (indexMatcher.matches()) {
                    if (indexMatcher.group().equals("-")) {
                        index = LAST_INDEX;
                    } else {
                        try {
                            int validInt = Integer.parseInt(indexMatcher.group());
                            index = validInt;
                        } catch (NumberFormatException ignore) {}
                    }
            }

            KeyRef keyRef = null;
            Matcher arrayKeyRefMatcher = VALID_ARRAY_KEY_REF.matcher(rawToken);
            if (arrayKeyRefMatcher.matches()) {
                keyRef = new KeyRef(
                    decodePath(arrayKeyRefMatcher.group(1)),
                    decodePath(arrayKeyRefMatcher.group(2))
                );
            }
            return new RefToken(decodePath(rawToken), index, keyRef);
        }

        public boolean isArrayIndex() {
            return index != null;
        }

        public boolean isArrayKeyRef() {
            return keyRef != null;
        }

        public int getIndex() {
            if (!isArrayIndex()) throw new IllegalStateException("Object operation on array index target");
            return index;
        }

        public KeyRef getKeyRef() {
            if (!isArrayKeyRef()) throw new IllegalStateException("Object operation on array key ref target");
            return keyRef;
        }

        public String getField() {
            return decodedToken;
        }

        @Override
        public String toString() {
            if (isArrayKeyRef()) {
                return encodePath(keyRef.key) + "=" + encodePath(keyRef.value);
            } else {
                return encodePath(decodedToken);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RefToken refToken = (RefToken) o;

            return decodedToken.equals(refToken.decodedToken);
        }

        @Override
        public int hashCode() {
            return decodedToken.hashCode();
        }
    }

    static class KeyRef {
        private String key;
        private String value;

        public KeyRef(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyRef keyRef = (KeyRef) o;

            return Objects.equals(key, keyRef.key) && Objects.equals(value, keyRef.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
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
