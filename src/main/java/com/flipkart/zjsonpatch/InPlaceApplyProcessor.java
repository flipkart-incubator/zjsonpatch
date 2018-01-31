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
import java.util.List;

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
    public void move(List<String> fromPath, List<String> toPath) {
        JsonNode parentNode = getParentNode(fromPath, Operation.MOVE);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        JsonNode valueNode = parentNode.isArray() ? parentNode.get(Integer.parseInt(field)) : parentNode.get(field);
        remove(fromPath);
        add(toPath, valueNode);
    }

    @Override
    public void copy(List<String> fromPath, List<String> toPath) {
        JsonNode parentNode = getParentNode(fromPath, Operation.COPY);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        JsonNode valueNode = parentNode.isArray() ? parentNode.get(Integer.parseInt(field)) : parentNode.get(field);
        JsonNode valueToCopy = valueNode != null ? valueNode.deepCopy() : null;
        add(toPath, valueToCopy);
    }

    @Override
    public void test(List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            error(Operation.TEST, "path is empty , path : ");
        } else {
            JsonNode parentNode = getParentNode(path, Operation.TEST);
            String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
            if (fieldToReplace.equals("") && path.size() == 1)
                if (target.equals(value)) {
                    target = value;
                } else {
                    error(Operation.TEST, "value mismatch");
                }
            else if (!parentNode.isContainerNode())
                error(Operation.TEST, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
            else if (parentNode.isArray()) {
                final ArrayNode target = (ArrayNode) parentNode;
                String idxStr = path.get(path.size() - 1);

                if ("-".equals(idxStr)) {
                    // see http://tools.ietf.org/html/rfc6902#section-4.1
                    if (!target.get(target.size() - 1).equals(value)) {
                        error(Operation.TEST, "value mismatch");
                    }
                } else {
                    int idx = arrayIndex(idxStr.replaceAll("\"", ""), target.size(), false);
                    if (!target.get(idx).equals(value)) {
                        error(Operation.TEST, "value mismatch");
                    }
                }
            } else {
                final ObjectNode target = (ObjectNode) parentNode;
                String key = path.get(path.size() - 1).replaceAll("\"", "");
                JsonNode actual = target.get(key);
                if (actual == null)
                    error(Operation.TEST, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
                else if (!actual.equals(value))
                    error(Operation.TEST, "value mismatch");
            }
        }
    }

    @Override
    public void add(List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            error(Operation.ADD, "path is empty , path : ");
        } else {
            JsonNode parentNode = getParentNode(path, Operation.ADD);
            String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
            if (fieldToReplace.equals("") && path.size() == 1)
                target = value;
            else if (!parentNode.isContainerNode())
                error(Operation.ADD, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
            else if (parentNode.isArray())
                addToArray(path, value, parentNode);
            else
                addToObject(path, parentNode, value);
        }
    }

    private void addToObject(List<String> path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.get(path.size() - 1).replaceAll("\"", "");
        target.set(key, value);
    }

    private void addToArray(List<String> path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        String idxStr = path.get(path.size() - 1);

        if ("-".equals(idxStr)) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            int idx = arrayIndex(idxStr.replaceAll("\"", ""), target.size(), false);
            target.insert(idx, value);
        }
    }

    @Override
    public void replace(List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            error(Operation.REPLACE, "path is empty");
        } else {
            JsonNode parentNode = getParentNode(path, Operation.REPLACE);
            String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
            if (isNullOrEmpty(fieldToReplace) && path.size() == 1)
                target = value;
            else if (parentNode.isObject())
                ((ObjectNode) parentNode).put(fieldToReplace, value);
            else if (parentNode.isArray())
                ((ArrayNode) parentNode).set(arrayIndex(fieldToReplace, parentNode.size() - 1, false), value);
            else
                error(Operation.REPLACE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
        }
    }

    @Override
    public void remove(List<String> path) {
        if (path.isEmpty()) {
            error(Operation.REMOVE, "path is empty");
        } else {
            JsonNode parentNode = getParentNode(path, Operation.REMOVE);
            String fieldToRemove = path.get(path.size() - 1).replaceAll("\"", "");
            if (parentNode.isObject())
                ((ObjectNode) parentNode).remove(fieldToRemove);
            else if (parentNode.isArray())
                ((ArrayNode) parentNode).remove(arrayIndex(fieldToRemove, parentNode.size() - 1, flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT)));
            else
                error(Operation.REMOVE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
        }
    }

    private void error(Operation forOp, String message) {
        throw new JsonPatchApplicationException("[" + forOp + " Operation] " + message);
    }

    private JsonNode getParentNode(List<String> fromPath, Operation forOp) {
        List<String> pathToParent = fromPath.subList(0, fromPath.size() - 1); // would never by out of bound, lets see
        JsonNode node = getNode(target, pathToParent, 1);
        if (node == null)
            error(forOp, "noSuchPath in source, path provided: " + PathUtils.getPathRepresentation(fromPath));
        return node;
    }

    private JsonNode getNode(JsonNode ret, List<String> path, int pos) {
        if (pos >= path.size()) {
            return ret;
        }
        String key = path.get(pos);
        if (ret.isArray()) {
            int keyInt = Integer.parseInt(key.replaceAll("\"", ""));
            JsonNode element = ret.get(keyInt);
            if (element == null)
                return null;
            else
                return getNode(ret.get(keyInt), path, ++pos);
        } else if (ret.isObject()) {
            if (ret.has(key)) {
                return getNode(ret.get(key), path, ++pos);
            }
            return null;
        } else {
            return ret;
        }
    }

    private int arrayIndex(String s, int max, boolean allowNoneExisting) {
        int index;
        try {
            index = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            throw new JsonPatchApplicationException("Object operation on array target");
        }
        if (index < 0) {
            throw new JsonPatchApplicationException("index Out of bound, index is negative");
        } else if (index > max) {
            if (!allowNoneExisting)
                throw new JsonPatchApplicationException("index Out of bound, index is greater than " + max);
        }
        return index;
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }
}
