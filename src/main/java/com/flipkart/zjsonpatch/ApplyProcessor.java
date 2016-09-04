package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import java.util.List;

class ApplyProcessor implements JsonPatchProcessor {

    private JsonNode target;

    ApplyProcessor(JsonNode target) {
        this.target = target.deepCopy();
    }

    public JsonNode result() {
        return target;
    }

    public void move(List<String> fromPath, List<String> toPath) {
        JsonNode parentNode = getParentNode(fromPath);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        JsonNode valueNode =  parentNode.isArray() ? parentNode.get(Integer.parseInt(field)) : parentNode.get(field);
        remove(fromPath);
        add(toPath, valueNode);
    }

    public void add(List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[ADD Operation] path is empty , path : ");
        } else {
            JsonNode parentNode = getParentNode(path);
            if (parentNode == null) {
                throw new JsonPatchApplicationException("[ADD Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
                if (fieldToReplace.equals("") && path.size() == 1)
                    target = value;
                else if (!parentNode.isContainerNode())
                    throw new JsonPatchApplicationException("[ADD Operation] parent is not a container in source, path provided : " + path + " | node : " + parentNode);
                else if (parentNode.isArray())
                    addToArray(path, value, parentNode);
                else
                    addToObject(path, parentNode, value);
            }
        }
    }

    private void addToObject(List<String> path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.get(path.size() - 1).replaceAll("\"", "");
        target.put(key, value);
    }

    private void addToArray(List<String> path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        String idxStr = path.get(path.size() - 1);

        if ("-".equals(idxStr)) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            Integer idx = Integer.parseInt(idxStr.replaceAll("\"", ""));
            if (idx < target.size()) {
                target.insert(idx, value);
            } else {
                if (idx == target.size()) {
                    target.add(value);
                } else {
                    throw new JsonPatchApplicationException("[ADD Operation] [addToArray] index Out of bound, index provided is higher than allowed, path " + path);
                }
            }
        }
    }

    public void replace(List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[Replace Operation] path is empty");
        } else {
            JsonNode parentNode = getParentNode(path);
            if (parentNode == null) {
                throw new JsonPatchApplicationException("[Replace Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
                if (Strings.isNullOrEmpty(fieldToReplace) && path.size() == 1)
                    target = value;
                else if (parentNode.isObject())
                    ((ObjectNode) parentNode).put(fieldToReplace, value);
                else if (parentNode.isArray())
                    ((ArrayNode) parentNode).set(Integer.parseInt(fieldToReplace), value);
                else
                    throw new JsonPatchApplicationException("[Replace Operation] noSuchPath in source, path provided : " + path);
            }
        }
    }

    public void remove(List<String> path) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[Remove Operation] path is empty");
        } else {
            JsonNode parentNode = getParentNode(path);
            if (parentNode == null) {
                throw new JsonPatchApplicationException("[Remove Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToRemove = path.get(path.size() - 1).replaceAll("\"", "");
                if (parentNode.isObject())
                    ((ObjectNode) parentNode).remove(fieldToRemove);
                else if (parentNode.isArray())
                    ((ArrayNode) parentNode).remove(Integer.parseInt(fieldToRemove));
                else
                    throw new JsonPatchApplicationException("[Remove Operation] noSuchPath in source, path provided : " + path);
            }
        }
    }

    private JsonNode getParentNode(List<String> fromPath) {
        List<String> pathToParent = fromPath.subList(0, fromPath.size() - 1); // would never by out of bound, lets see
        return getNode(target, pathToParent, 1);
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
}
