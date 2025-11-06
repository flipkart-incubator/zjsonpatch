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
import com.flipkart.zjsonpatch.mapping.JsonTokenWrapper;

import java.util.EnumMap;
import java.util.Map;

enum NodeType {
    /**
     * Array nodes
     */
    ARRAY("array"),
    /**
     * Boolean nodes
     */
    BOOLEAN("boolean"),
    /**
     * Integer nodes
     */
    INTEGER("integer"),
    /**
     * Number nodes (ie, decimal numbers)
     */
    NULL("null"),
    /**
     * Object nodes
     */
    NUMBER("number"),
    /**
     * Null nodes
     */
    OBJECT("object"),
    /**
     * String nodes
     */
    STRING("string");

    /**
     * The name for this type, as encountered in a JSON schema
     */
    private final String name;

    private static final Map<JsonTokenWrapper, NodeType> TOKEN_MAP
            = new EnumMap<JsonTokenWrapper, NodeType>(JsonTokenWrapper.class);

    static {
        TOKEN_MAP.put(JsonTokenWrapper.START_ARRAY, ARRAY);
        TOKEN_MAP.put(JsonTokenWrapper.VALUE_TRUE, BOOLEAN);
        TOKEN_MAP.put(JsonTokenWrapper.VALUE_FALSE, BOOLEAN);
        TOKEN_MAP.put(JsonTokenWrapper.VALUE_NUMBER_INT, INTEGER);
        TOKEN_MAP.put(JsonTokenWrapper.VALUE_NUMBER_FLOAT, NUMBER);
        TOKEN_MAP.put(JsonTokenWrapper.VALUE_NULL, NULL);
        TOKEN_MAP.put(JsonTokenWrapper.START_OBJECT, OBJECT);
        TOKEN_MAP.put(JsonTokenWrapper.VALUE_STRING, STRING);

    }

    NodeType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NodeType getNodeType(final JsonNodeWrapper node) {
        final JsonTokenWrapper token = node.asToken();
        final NodeType ret = TOKEN_MAP.get(token);
        if (ret == null) throw new NullPointerException("unhandled token type " + token);
        return ret;
    }
}
