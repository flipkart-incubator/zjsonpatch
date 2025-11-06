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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single JSON Pointer reference token.
 */
public class RefToken {
    private final String decodedToken;
    private final Integer index;
    private final KeyRef keyRef;

    public RefToken(String decodedToken, Integer arrayIndex, KeyRef arrayKeyRef) {
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
                    index = JsonPointer.LAST_INDEX;
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
