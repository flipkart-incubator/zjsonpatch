package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public class JsonPatch {

    public static JsonNode apply(JsonNode patch, JsonNode source) {
        Iterator<JsonNode> operations = patch.iterator();
        JsonNode ret = source.deepCopy();
        while (operations.hasNext()) {
            JsonNode jsonNode = operations.next();
            Operation operation = Operation.valueOf(jsonNode.get("o").toString().replaceAll("\"", ""));
            List<String> path = getPath(jsonNode.get("p"));
            List<String> toPath = null;
            if (Operation.MOVE.equals(operation)) {
                toPath = getPath(jsonNode.get("tp"));
            }
            JsonNode value = null;
            if (!Operation.REMOVE.equals(operation)) {
                value = jsonNode.get("v");
            }

            switch (operation) {
                case REMOVE:
                    remove(ret, path);
                    break;
                case REPLACE:
                    ret = replace(ret, path, value);
                    break;
                case ADD:
                    add(ret, path, value);
                    break;
                case MOVE:
                    move(ret, path, toPath, value);
                    break;
            }
        }
        return ret;
    }

    private static void move(JsonNode node, List<String> fromPath, List<String> toPath, JsonNode value) {
        remove(node, fromPath);
        add(node, toPath, value);
    }

    private static void add(JsonNode node, List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new RuntimeException("[ADD Operation] path is empty , path : ");
        } else {
            List<String> pathToParent = path.subList(0, path.size() - 1); // would never by out of bound, lets see
            JsonNode parentNode = getNode(node, pathToParent, 1);
            if (parentNode == null) {
                throw new RuntimeException("[ADD Operation] noSuchPath in source, path provided : " + path);
            } else {
                if (!parentNode.isContainerNode()) {
                    throw new RuntimeException("[ADD Operation] parent is not a container in source, path provided : " + path + " | node : " + parentNode);
                } else {
                    if (parentNode.isArray()) {
                        addToArray(path, value, parentNode);
                    } else {
                        addToObject(path, parentNode, value);
                    }
                }
            }
        }
    }

    private static void addToObject(List<String> path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.get(path.size() - 1).replaceAll("\"", "");
        target.put(key, value);
    }

    private static void addToArray(List<String> path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        String idxStr = path.get(path.size() - 1);
        Integer idx = Integer.parseInt(idxStr.replaceAll("\"", ""));
        if (idx < target.size()) {
            target.insert(idx, value);
        } else {
            if (idx == target.size()) {
                target.add(value);
            } else {
                throw new RuntimeException("[ADD Operation] [addToArray] index Out of bound, index provided is higher than allowed, path " + path);
            }
        }
    }

    private static JsonNode replace(JsonNode node, List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new RuntimeException("[Replace Operation] path is empty");
        } else {
            List<String> pathToParent = path.subList(0, path.size() - 1); // would never by out of bound, lets see
            JsonNode parentNode = getNode(node, pathToParent, 1);
            if (parentNode == null) {
                throw new RuntimeException("[Replace Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
                if (Strings.isNullOrEmpty(fieldToReplace) && path.size() == 1) {
                    return value;
                }
                if (parentNode.isObject())
                    ((ObjectNode) parentNode).put(fieldToReplace, value);
                else
                    ((ArrayNode) parentNode).set(Integer.parseInt(fieldToReplace), value);
            }
            return node;
        }
    }

    private static void remove(JsonNode node, List<String> path) {
        if (path.isEmpty()) {
            throw new RuntimeException("[Remove Operation] path is empty");
        } else {
            List<String> pathToParent = path.subList(0, path.size() - 1); // would never be out of bound, lets see
            JsonNode parentNode = getNode(node, pathToParent, 1);
            if (parentNode == null) {
                throw new RuntimeException("[Remove Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToRemove = path.get(path.size() - 1).replaceAll("\"", "");
                if (parentNode.isObject())
                    ((ObjectNode) parentNode).remove(fieldToRemove);
                else
                    ((ArrayNode) parentNode).remove(Integer.parseInt(fieldToRemove));
            }
        }
    }

    private static JsonNode getNode(JsonNode ret, List<String> path, int pos) {
        if (pos >= path.size()) {
            return ret;
        }
        String key = path.get(pos);
        if (ret.isArray()) {
            int keyInt = Integer.parseInt(key.replaceAll("\"", ""));
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

    private static List<String> getPath(JsonNode path) {
        List<String> strPath = new ArrayList<String>();
        strPath.add(""); //marker for root
        Iterator<JsonNode> iterator = path.iterator();
        while (iterator.hasNext()) {
            strPath.add(iterator.next().toString().replaceAll("\"", ""));
        }
        return strPath;
    }
}
