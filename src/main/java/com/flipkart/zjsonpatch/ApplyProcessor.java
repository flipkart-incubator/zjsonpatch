package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

class ApplyProcessor implements JsonPatchProcessor {

    private JsonNode target;

    ApplyProcessor(JsonNode target) {
        this.target = target;
    }

    public JsonNode result() {
        return target;
    }

    @Override
    public JsonNode add(List<String> path, JsonNode value) {
        JsonNode parentNode = getParentNode(path);
        if (parentNode == null) {
            throw new JsonPatchApplicationException(
                    "[Add Operation] noSuchPath in source, path provided : " + path);
        }
        String fieldToReplace = path.get(path.size() - 1);
        if (fieldToReplace.isEmpty() && path.size() == 1) {
            target = value;
        } else if (!parentNode.isContainerNode()) {
            throw new JsonPatchApplicationException(
                    "[Add Operation] parent is not a container in source, path provided : " + path +
                            " | node : " + parentNode);
        } else if (parentNode.isArray()) {
            addToArray(path, value, parentNode);
        } else {
            addToObject(path, parentNode, value);
        }
        return value;
    }

    private void addToObject(List<String> path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.get(path.size() - 1);
        target.set(key, value);
    }

    private void addToArray(List<String> path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        String idxStr = path.get(path.size() - 1);

        if ("-".equals(idxStr)) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            int idx = arrayIndex(idxStr, target.size());
            target.insert(idx, value);
        }
    }

    @Override
    public JsonNode test(List<String> path, JsonNode value) {
        JsonNode node = getNode(target, path, 1);
        if (node == null) {
            throw new JsonPatchApplicationException(
                    "[Test Operation] noSuchPath in source, path provided : " + path);
        } else if (!node.equals(value)) {
            throw new JsonPatchApplicationException(
                    "[Test Operation] value differs from expectations : " + path + 
                    " | value : " + value + " | node : " + node);
        }
        return value;
    }

    @Override
    public JsonNode replace(List<String> path, JsonNode value) {
        JsonNode parentNode = getParentNode(path);
        if (parentNode == null) {
            throw new JsonPatchApplicationException(
                    "[Replace Operation] noSuchPath in source, path provided : " + path);
        }
        String fieldToReplace = path.get(path.size() - 1);
        if (fieldToReplace.isEmpty() && path.size() == 1) {
            target = value;
        } else if (parentNode.isObject()) {
            ((ObjectNode) parentNode).set(fieldToReplace, value);
        } else if (parentNode.isArray()) {
            ((ArrayNode) parentNode).set(arrayIndex(fieldToReplace, parentNode.size() - 1), value);
        } else {
            throw new JsonPatchApplicationException(
                    "[Replace Operation] noSuchPath in source, path provided : " + path);
        }
        return value;
    }

    @Override
    public JsonNode remove(List<String> path) {
        JsonNode parentNode = getParentNode(path);
        if (parentNode == null) {
            throw new JsonPatchApplicationException(
                    "[Remove Operation] noSuchPath in source, path provided : " + path);
        }
        String fieldToRemove = path.get(path.size() - 1);
        if (parentNode.isObject()) {
            return ((ObjectNode) parentNode).remove(fieldToRemove);
        } else if (parentNode.isArray()) {
            return ((ArrayNode) parentNode).remove(arrayIndex(fieldToRemove, parentNode.size() - 1));
        }
        throw new JsonPatchApplicationException(
                "[Remove Operation] noSuchPath in source, path provided : " + path);
    }

    @Override
    public JsonNode move(List<String> fromPath, List<String> toPath) {
        return add(toPath, remove(fromPath));
    }

    @Override
    public JsonNode copy(List<String> fromPath, List<String> toPath) {
        return add(toPath, getNode(target, fromPath, 1));
    }

    private JsonNode getParentNode(List<String> path) {
        return getNode(target, path.subList(0, path.size() - 1), 1);
    }

    private JsonNode getNode(JsonNode node, List<String> path, int index) {
        if (index >= path.size()) {
            return node;
        }
        String key = path.get(index);
        if (node.isArray()) {
            int keyInt = Integer.parseInt(key);
            JsonNode element = node.get(keyInt);
            if (element == null)
                return null;
            else
                return getNode(node.get(keyInt), path, ++index);
        } else if (node.isObject()) {
            if (node.has(key)) {
                return getNode(node.get(key), path, ++index);
            }
            return null;
        } else {
            return node;
        }
    }

    private int arrayIndex(String s, int max) {
        int index = Integer.parseInt(s);
        if (index < 0) {
            throw new JsonPatchApplicationException("index Out of bound, index is negative");
        } else if (index > max) {
            throw new JsonPatchApplicationException("index Out of bound, index is greater than " + max);
        }
        return index;
    }
}
