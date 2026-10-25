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

/**
 * User: holograph
 * Date: 03/08/16
 */
public class JsonPatchApplicationException extends RuntimeException {
    final Operation operation;
    final AbstractJsonPointer path;
    final Object value;

    public JsonPatchApplicationException(String message, Operation operation, AbstractJsonPointer path) {
        this(message, operation, path, null);
    }

    /**
     * @param value the underlying JSON node value that was being applied (e.g. from
     *              {@code JsonNodeWrapper.getUnderlyingNode()}), or {@code null} if not applicable
     */
    public JsonPatchApplicationException(String message, Operation operation, AbstractJsonPointer path, Object value) {
        super(message);
        this.operation = operation;
        this.path = path;
        this.value = value;
    }

    public Operation getOperation() {
    	return operation;
    }

    public AbstractJsonPointer getPath() {
    	return path;
    }

    /**
     * Returns the Jackson {@code JsonNode} that was being applied when this exception was thrown,
     * obtained via {@code JsonNodeWrapper.getUnderlyingNode()} at the throw site. Returns
     * {@code null} for operations that have no incoming value (e.g. {@code remove}).
     * Cast to {@code com.fasterxml.jackson.databind.JsonNode} (Jackson 2) or
     * {@code tools.jackson.databind.JsonNode} (Jackson 3) depending on the version in use.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (operation != null) sb.append('[').append(operation).append(" Operation] ");
        sb.append(getMessage());
        if (path != null) sb.append(" at ").append(path.isRoot() ? "root" : path);
        if (value != null) sb.append(" (value: ").append(value).append(')');
        return sb.toString();
    }

}
