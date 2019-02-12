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
        JsonNode parentNode = getParentNode(fromPath, Operation.MOVE);
        JsonPointer.RefToken ind = fromPath.get(fromPath.size() - 1);
        JsonNode valueNode = parentNode.isArray() ? parentNode.get(ind.getIndex()) : parentNode.get(ind.getField());
        remove(fromPath);
        add(toPath, valueNode);
    }

    @Override
    public void copy(JsonPointer fromPath, JsonPointer toPath) {
        JsonNode parentNode = getParentNode(fromPath, Operation.COPY);
        JsonPointer.RefToken ind = fromPath.get(fromPath.size() - 1);
        JsonNode valueNode = parentNode.isArray() ? parentNode.get(ind.getIndex()) : parentNode.get(ind.getField());
        JsonNode valueToCopy = valueNode != null ? valueNode.deepCopy() : null;
        add(toPath, valueToCopy);
    }

    @Override
    public void test(JsonPointer path, JsonNode value) {
        JsonNode parentNode = getParentNode(path, Operation.TEST);
        if (path.isRoot())
            if (target.equals(value)) {
                target = value;
            } else {
                error(Operation.TEST, "value mismatch");
            }
        else if (!parentNode.isContainerNode())
            error(Operation.TEST, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
        else if (parentNode.isArray()) {
            final ArrayNode target = (ArrayNode) parentNode;
            JsonPointer.RefToken token = path.get(path.size() - 1);

            if (token.getIndex() == JsonPointer.LAST_INDEX) {
                // see http://tools.ietf.org/html/rfc6902#section-4.1
                if (!target.get(target.size() - 1).equals(value)) {
                    error(Operation.TEST, "value mismatch");
                }
            } else {
                validateIndex(token.getIndex(), target.size(), false);
                if (!target.get(token.getIndex()).equals(value)) {
                    error(Operation.TEST, "value mismatch");
                }
            }
        } else {
            final ObjectNode target = (ObjectNode) parentNode;
            String key = path.get(path.size() - 1).getField();
            JsonNode actual = target.get(key);
            if (actual == null)
                error(Operation.TEST, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
            else if (!actual.equals(value))
                error(Operation.TEST, "value mismatch");
        }
    }

    @Override
    public void add(JsonPointer path, JsonNode value) {
        JsonNode parentNode = getParentNode(path, Operation.ADD);
        if (path.isRoot())
            target = value;
        else if (!parentNode.isContainerNode())
            error(Operation.ADD, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
        else if (parentNode.isArray())
            addToArray(path, value, parentNode);
        else
            addToObject(path, parentNode, value);
    }

    private void addToObject(JsonPointer path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.get(path.size() - 1).getField();
        target.set(key, value);
    }

    private void addToArray(JsonPointer path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        int idx = path.get(path.size() - 1).getIndex();

        if (idx == JsonPointer.LAST_INDEX) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            validateIndex(idx, target.size(), false);
            target.insert(idx, value);
        }
    }

    @Override
    public void replace(JsonPointer path, JsonNode value) {
        if (path.isRoot()) {
            target = value;
            return;
        }

        JsonNode parentNode = getParentNode(path, Operation.REPLACE);
        JsonPointer.RefToken token = path.get(path.size() - 1);
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
        JsonPointer.RefToken token = path.get(path.size() - 1);
        if (parentNode.isObject())
            ((ObjectNode) parentNode).remove(token.getField());
        else if (parentNode.isArray()) {
            validateIndex(token.getIndex(), parentNode.size() - 1, flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT));
            ((ArrayNode) parentNode).remove(token.getIndex());
        } else
            error(Operation.REMOVE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
    }

    private void error(Operation forOp, String message) {
        throw new JsonPatchApplicationException("[" + forOp + " Operation] " + message);
    }

    private JsonNode getParentNode(JsonPointer fromPath, Operation forOp) {
        JsonPointer pathToParent = fromPath.getParent();
        JsonNode node = getNode(target, pathToParent, 0, forOp);
        if (node == null)
            error(forOp, "noSuchPath in source, path provided: " + PathUtils.getPathRepresentation(fromPath));
        return node;
    }

    // TODO move to JsonPointer
    private JsonNode getNode(JsonNode ret, JsonPointer path, int pos, Operation forOp) {
        if (pos >= path.size()) {
            return ret;
        }
        JsonPointer.RefToken token = path.get(pos);
        if (ret.isArray()) {
            if (token.isArrayIndex())
                error(forOp, "Object operation on array target");   // TODO improve
            JsonNode element = ret.get(token.getIndex());
            if (element == null)
                return null;
            else
                return getNode(element, path, ++pos, forOp);
        } else if (ret.isObject()) {
            if (ret.has(token.getField())) {
                return getNode(ret.get(token.getField()), path, ++pos, forOp);
            }
            return null;
        } else {
            return ret;
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
