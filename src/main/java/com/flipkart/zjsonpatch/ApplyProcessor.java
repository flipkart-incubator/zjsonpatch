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

    @Override
    public void move(List<String> fromPath, List<String> toPath) {
        JsonNode parentNode = getParentNode(fromPath);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        JsonNode valueNode =  parentNode.isArray() ? parentNode.get(Integer.parseInt(field)) : parentNode.get(field);
        remove(fromPath);
        add(toPath, valueNode);
    }

    @Override
    public void copy(List<String> fromPath, List<String> toPath) {
        JsonNode parentNode = getParentNode(fromPath);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        JsonNode valueNode =  parentNode.isArray() ? parentNode.get(Integer.parseInt(field)) : parentNode.get(field);
        add(toPath, valueNode);
    }

    @Override
    public void test(List<String> path, JsonNode value) {
        if (path.isEmpty()) {
            throw new JsonPatchApplicationException("[TEST Operation] path is empty , path : ");
        } else {
            JsonNode parentNode = getParentNode(path);
            if (parentNode == null) {
                throw new JsonPatchApplicationException("[TEST Operation] noSuchPath in source, path provided : " + path);
            } else {
                String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
                if (fieldToReplace.equals("") && path.size() == 1)
                    target = value;
                else if (!parentNode.isContainerNode())
                    throw new JsonPatchApplicationException("[TEST Operation] parent is not a container in source, path provided : " + path + " | node : " + parentNode);
                else if (parentNode.isArray()) {
                    final ArrayNode target = (ArrayNode) parentNode;
                    String idxStr = path.get(path.size() - 1);

                    if ("-".equals(idxStr)) {
                        // see http://tools.ietf.org/html/rfc6902#section-4.1
                        if(!target.get(target.size()-1).equals(value)){
                            throw new JsonPatchApplicationException("[TEST Operation] value mismatch");
                        }
                    } else {
                        int idx = arrayIndex(idxStr.replaceAll("\"", ""), target.size());
                        if(!target.get(idx).equals(value)){
                            throw new JsonPatchApplicationException("[TEST Operation] value mismatch");
                        }
                    }
                }
                else {
                    final ObjectNode target = (ObjectNode) parentNode;
                    String key = path.get(path.size() - 1).replaceAll("\"", "");
                    if(!target.get(key).equals(value)){
                        throw new JsonPatchApplicationException("[TEST Operation] value mismatch");
                    }
                }
            }
        }
    }

    @Override
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
        target.set(key, value);
    }

    private void addToArray(List<String> path, JsonNode value, JsonNode parentNode) {
        final ArrayNode target = (ArrayNode) parentNode;
        String idxStr = path.get(path.size() - 1);

        if ("-".equals(idxStr)) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            int idx = arrayIndex(idxStr.replaceAll("\"", ""), target.size());
            target.insert(idx, value);
        }
    }

    @Override
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
                    ((ArrayNode) parentNode).set(arrayIndex(fieldToReplace, parentNode.size() - 1), value);
                else
                    throw new JsonPatchApplicationException("[Replace Operation] noSuchPath in source, path provided : " + path);
            }
        }
    }

    @Override
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
                    ((ArrayNode) parentNode).remove(arrayIndex(fieldToRemove, parentNode.size() - 1));
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
