package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public final class JsonPatch {

    private JsonPatch() {}

    private final static JsonNode getPatchAttr(JsonNode jsonNode, String attr) {
        JsonNode child = jsonNode.get(attr);
        if (child == null)
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
        return child;
    }

    public static JsonNode apply(JsonNode patch, JsonNode source) {
        Iterator<JsonNode> operations = patch.iterator();
        JsonNode ret = source.deepCopy();
        while (operations.hasNext()) {
            JsonNode jsonNode = operations.next();
            if (!jsonNode.isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchAttr(jsonNode, Constants.OP).toString().replaceAll("\"", ""));
            List<String> path = getPath(getPatchAttr(jsonNode, Constants.PATH));
            List<String> fromPath = null;
            if (Operation.MOVE.equals(operation)) {
                fromPath = getPath(getPatchAttr(jsonNode, Constants.FROM));
            }
            JsonNode value = null;
            if (!Operation.REMOVE.equals(operation) && !Operation.MOVE.equals(operation)) {
                value = getPatchAttr(jsonNode, Constants.VALUE);
            }

            switch (operation) {
                case REMOVE:
                    remove(ret, path);
                    break;
                case REPLACE:
                    ret = replace(ret, path, value);
                    break;
                case ADD:
                    ret = add(ret, path, value);
                    break;
                case MOVE:
                    ret = move(ret, fromPath, path);
                    break;
            }
        }
        return ret;
    }

    private static JsonNode move(JsonNode node, List<String> fromPath, List<String> toPath) {
        JsonNode parentNode = getParentNode(node, fromPath);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        JsonNode valueNode =  parentNode.isArray() ? parentNode.get(Integer.parseInt(field)) : parentNode.get(field);
        remove(node, fromPath);
        return add(node, toPath, valueNode);
    }

    private static JsonNode add(JsonNode node, List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[ADD Operation] path is empty , path : ");
        } else {
            JsonNode parentNode = getParentNode(node, path);
            if (parentNode == null) {
                throw new JsonPatchApplicationException("[ADD Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
                if (fieldToReplace.equals("") && path.size() == 1) {
                    return value;
                }
                if (!parentNode.isContainerNode()) {
                    throw new JsonPatchApplicationException("[ADD Operation] parent is not a container in source, path provided : " + path + " | node : " + parentNode);
                } else {
                    if (parentNode.isArray()) {
                        addToArray(path, value, parentNode);
                    } else {
                        addToObject(path, parentNode, value);
                    }
                }
            }
        }
        return node;
    }

    private static void addToObject(List<String> path, JsonNode node, JsonNode value) {
        final ObjectNode target = (ObjectNode) node;
        String key = path.get(path.size() - 1).replaceAll("\"", "");
        target.put(key, value);
    }

    private static void addToArray(List<String> path, JsonNode value, JsonNode parentNode) {
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

    private static JsonNode replace(JsonNode node, List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[Replace Operation] path is empty");
        } else {
            JsonNode parentNode = getParentNode(node, path);
            if (parentNode == null) {
                throw new JsonPatchApplicationException("[Replace Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
                if ((fieldToReplace == null || fieldToReplace.isEmpty()) && path.size() == 1) {
                    return value;
                }
                if (parentNode.isObject())
                    ((ObjectNode) parentNode).put(fieldToReplace, value);
                else if (parentNode.isArray())
                    ((ArrayNode) parentNode).set(Integer.parseInt(fieldToReplace), value);
                else
                    throw new JsonPatchApplicationException("[Replace Operation] noSuchPath in source, path provided : " + path);
            }
            return node;
        }
    }

    private static void remove(JsonNode node, List<String> path) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[Remove Operation] path is empty");
        } else {
            JsonNode parentNode = getParentNode(node, path);
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

    private static JsonNode getParentNode(JsonNode node, List<String> fromPath) {
        List<String> pathToParent = fromPath.subList(0, fromPath.size() - 1); // would never by out of bound, lets see
        return getNode(node, pathToParent, 1);
    }

    private static JsonNode getNode(JsonNode ret, List<String> path, int pos) {
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

    private final static class DecodePathFunction implements Function<String, String> {
        @Override
        public String apply(String path) {
            return path.replaceAll("~1", "/").replaceAll("~0", "~"); // see http://tools.ietf.org/html/rfc6901#section-4
        }
    }

    private static final DecodePathFunction DECODE_PATH_FUNCTION = new DecodePathFunction();

    private static List<String> getPath(JsonNode path) {
        /*
         * List<String> paths = Splitter.on('/').splitToList(path.toString().replaceAll("\"", ""));
         * return Lists.newArrayList(Iterables.transform(paths, DECODE_PATH_FUNCTION));
         */
        String pathString = path.toString().replaceAll("\"", "");
        String[] paths = pathString.split("/", -1);
        List<String> result = Arrays.stream(paths).map(DECODE_PATH_FUNCTION).collect(Collectors.toList());
        return result;
    }
}
