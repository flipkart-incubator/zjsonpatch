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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.EnumSet;
import java.util.Objects;

class InPlaceApplyProcessor implements JsonPatchProcessor {

    private JsonNode target;
    private EnumSet<CompatibilityFlags> flags;

    InPlaceApplyProcessor(JsonNode target) {
        this(target, CompatibilityFlags.defaults());
    }

    InPlaceApplyProcessor(JsonNode target, EnumSet<CompatibilityFlags> flags) {
        this.target = target;
        this.flags = flags;
    }

    public JsonNode result() {
        return target;
    }

    @Override
    public void move(JsonPointer fromPath, JsonPointer toPath) {
        JsonNode valueNode = getNode(fromPath, Operation.MOVE);
        remove(fromPath);
        set(toPath, valueNode, Operation.MOVE);
    }

    @Override
    public void copy(JsonPointer fromPath, JsonPointer toPath) {
        JsonNode valueNode = getNode(fromPath, Operation.COPY);
        JsonNode valueToCopy = valueNode != null ? valueNode.deepCopy() : null;
        set(toPath, valueToCopy, Operation.COPY);
    }

    @Override
    public void test(JsonPointer path, JsonNode value) {
        JsonNode valueNode = getNode(path, Operation.TEST);
        if (!Objects.equals(valueNode, value))
            error(Operation.TEST, "value mismatch");
    }

    @Override
    public void add(JsonPointer path, JsonNode value) {
        set(path, value, Operation.ADD);
    }

    @Override
    public void replace(JsonPointer path, JsonNode value) {
        if (path.isRoot()) {
            target = value;
            return;
        }

        JsonNode parentNode = getParentNode(path, Operation.REPLACE);
        JsonPointer.RefToken token = path.last();
        if (parentNode.isObject() && parentNode.has(token.getField())) {
            ((ObjectNode) parentNode).replace(token.getField(), value);
        } else if (parentNode.isArray()) {
            validateIndex(token.getIndex(), parentNode.size() - 1, false);
            ((ArrayNode) parentNode).set(token.getIndex(), value);
        } else
            error(Operation.REPLACE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
    }

    @Override
    public void remove(JsonPointer path) {
        if (path.isRoot()) {
            error(Operation.REMOVE, "path is empty");
            return;
        }
        JsonNode parentNode = getParentNode(path, Operation.REMOVE);
        JsonPointer.RefToken token = path.last();
        if (parentNode.isObject())
            ((ObjectNode) parentNode).remove(token.getField());
        else if (parentNode.isArray()) {
            validateIndex(token.getIndex(), parentNode.size() - 1, flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT));
            ((ArrayNode) parentNode).remove(token.getIndex());
        } else
            error(Operation.REMOVE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
    }



    private void set(JsonPointer path, JsonNode value, Operation forOp) {
        JsonNode parentNode = getParentNode(path, forOp);
        if (path.isRoot())
            target = value;
        else if (!parentNode.isContainerNode())
            error(forOp, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
        else if (parentNode.isArray())
            addToArray(path, value, parentNode);
        else
            addToObject(path, parentNode, value);
    }

    private void addToObject(JsonPointer path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.last().getField();
        target.set(key, value);
    }

    private void addToArray(JsonPointer path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        int idx = path.last().getIndex();

        if (idx == JsonPointer.LAST_INDEX) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            validateIndex(idx, target.size(), false);
            target.insert(idx, value);
        }
    }

    private void error(Operation forOp, String message) {
        throw new JsonPatchApplicationException("[" + forOp + " Operation] " + message);
    }

    private JsonNode getParentNode(JsonPointer path, Operation forOp) {
        try {
            return path.getParent().evaluate(target);
        }
        catch(JsonPointerEvaluationException e) {
            error(forOp, e.getMessage() + " at " + path);
            return null;    // Dead code but one has to appease the Java type system gods
        }
    }

    private JsonNode getNode(JsonPointer path, Operation forOp) {
        try {
            return path.evaluate(target);
        }
        catch(JsonPointerEvaluationException e) {
            error(forOp, e.getMessage() + " at " + e.getPath());
            return null;    // Dead code but one has to appease the Java type system gods
        }
    }

    private void validateIndex(int index, int max, boolean allowNoneExisting) {
        if (index < 0) {
            throw new JsonPatchApplicationException("index Out of bound, index is negative");
        } else if (index > max) {
            if (!allowNoneExisting)
                throw new JsonPatchApplicationException("index Out of bound, index is greater than " + max);
        }
    }
}
