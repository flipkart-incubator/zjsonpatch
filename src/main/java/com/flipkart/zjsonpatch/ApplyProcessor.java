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

    private JsonNode getParentNode(List<String> fromPath) {
        List<String> pathToParent = fromPath.subList(0, fromPath.size() - 1); 
        return getNode(target, pathToParent, 1);
    }

    private JsonNode getNode(JsonNode ret, List<String> path, int index) {
        if (index >= path.size()) {
            return ret;
        }
        String key = path.get(index);
        if (ret.isArray()) {
            int keyInt = Integer.parseInt(key);
            JsonNode element = ret.get(keyInt);
            if (element == null)
                return null;
            else
                return getNode(ret.get(keyInt), path, ++index);
        } else if (ret.isObject()) {
            if (ret.has(key)) {
                return getNode(ret.get(key), path, ++index);
            }
            return null;
        } else {
            return ret;
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
