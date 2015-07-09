package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
public class JsonDiff {

    public static JsonNode asJson(final JsonNode source, final JsonNode target) {
        final List<Diff> diffs = new ArrayList<Diff>();
        List<Object> path = new LinkedList<Object>();
        /**
         * generating diffs in the order of their occurrence
         */
        generateDiffs(diffs, path, source, target);
        /**
         * Merging remove & add to move operation
         */
        compactDiffs(diffs);

        return getJsonNodes(diffs);
    }

    /**
     * This method merge 2 diffs ( remove then add ) with same value into one Move operation, all the core logic resides here only
     */
    private static void compactDiffs(List<Diff> diffs) {
        for (int i = 0; i < diffs.size(); i++) { //N*N, lets see how to optimize it
            Diff rmDiff = diffs.get(i);
            if (Operation.REMOVE.equals(rmDiff.getOperation())) {
                for (int j = i + 1; j < diffs.size(); j++) {
                    Diff addDiff = diffs.get(j);
                    if (Operation.ADD.equals(addDiff.getOperation())
                            && addDiff.getValue().equals(rmDiff.getValue())) {
                        Diff moveDiff = getRelativeMoveDiff(i, rmDiff, j, addDiff, diffs);
                        diffs.remove(j);
                        diffs.set(i, moveDiff);
                        break;
                    }
                }
            }
        }
    }

    /**
     * This method computes the relative path to use in MOVE operation
     */
    private static Diff getRelativeMoveDiff(int i, Diff rmDiff, int j, Diff addDiff, List<Diff> diffs) {
        computeRelativePath(addDiff.getPath(), i + 1, j - 1, diffs);
        return new Diff(Operation.MOVE, rmDiff.getPath(), addDiff.getValue(), addDiff.getPath());
    }

    //Note : only to be used for arrays
    //Finds the longest common Ancestor ending at Array
    private static void computeRelativePath(List<Object> path, int startIdx, int endIdx, List<Diff> diffs) {
        List<Integer> counters = new ArrayList<Integer>();

        resetCounters(counters, path.size());

        for (int i = startIdx; i <= endIdx; i++) {
            Diff diff = diffs.get(i);
            //Adjust relative path according to #ADD and #Remove
            if (Operation.ADD.equals(diff.getOperation()) || Operation.REMOVE.equals(diff.getOperation())) {
                updatePath(path, diff, counters);
            }
        }
        updatePathWithCounters(counters, path);
    }

    private static void resetCounters(List<Integer> counters, int size) {
        for (int i = 0; i < size; i++) {
            counters.add(0);
        }
    }

    private static void updatePathWithCounters(List<Integer> counters, List<Object> path) {
        for (int i = 0; i < counters.size(); i++) {
            int value = counters.get(i);
            if (value != 0) {
                Integer currValue = Integer.parseInt(path.get(i).toString());
                path.set(i, String.valueOf(currValue + value));
            }
        }
    }

    private static void updatePath(List<Object> path, Diff pseudo, List<Integer> counters) {
        //find longest common prefix of both the paths

        if (pseudo.getPath().size() <= path.size()) {
            int idx = -1;
            for (int i = 0; i < pseudo.getPath().size() - 1; i++) {
                if (pseudo.getPath().get(i).equals(path.get(i))) {
                    idx = i;
                } else {
                    break;
                }
            }
            if (idx == pseudo.getPath().size() - 2) {
                if (pseudo.getPath().get(pseudo.getPath().size() - 1) instanceof Integer) {
                    updateCounters(pseudo, pseudo.getPath().size() - 1, counters);
                }
            }
        }
    }

    private static void updateCounters(Diff pseudo, int idx, List<Integer> counters) {
        if (Operation.ADD.equals(pseudo.getOperation())) {
            counters.set(idx, counters.get(idx) - 1);
        } else {
            if (Operation.REMOVE.equals(pseudo.getOperation())) {
                counters.set(idx, counters.get(idx) + 1);
            }
        }
    }

    private static ArrayNode getJsonNodes(List<Diff> diffs) {
        JsonNodeFactory FACTORY = JsonNodeFactory.instance;
        final ArrayNode patch = FACTORY.arrayNode();
        for (Diff diff : diffs) {
            ObjectNode jsonNode = getJsonNode(FACTORY, diff);
            patch.add(jsonNode);
        }
        return patch;
    }

    private static ObjectNode getJsonNode(JsonNodeFactory FACTORY, Diff diff) {
        ObjectNode jsonNode = FACTORY.objectNode();
        jsonNode.put(Constants.OP, diff.getOperation().name());
        jsonNode.put(Constants.PATH, getArrayNodeRepresentation(diff.getPath()));
        if (Operation.MOVE.equals(diff.getOperation())) {
            jsonNode.put(Constants.FROM, getArrayNodeRepresentation(diff.getPath())); //required {from} only in case of Move Operation
            jsonNode.put(Constants.PATH, getArrayNodeRepresentation(diff.getToPath()));  // destination Path
        }
        if (!Operation.REMOVE.equals(diff.getOperation())) { // setting only for Non-Remove operation
            jsonNode.put(Constants.VALUE, diff.getValue());
        }
        return jsonNode;
    }

    private static ArrayNode getArrayNodeRepresentation(List<Object> path) {
        JsonNodeFactory FACTORY = JsonNodeFactory.instance;
        ArrayNode arrayNode = FACTORY.arrayNode();
        for (Object object : path) {
            if (object instanceof Integer) {
                arrayNode.add((Integer) object);
            } else {
                arrayNode.add(object.toString());
            }
        }
        return arrayNode;
    }

    private static void generateDiffs(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        if (!source.equals(target)) {
            final NodeType sourceType = NodeType.getNodeType(source);
            final NodeType targetType = NodeType.getNodeType(target);

            if (sourceType == NodeType.ARRAY && targetType == NodeType.ARRAY) {
                //both are arrays
                compareArray(diffs, path, source, target);
            } else if (sourceType == NodeType.OBJECT && targetType == NodeType.OBJECT) {
                //both are json
                compareObjects(diffs, path, source, target);
            } else {
                //can be replaced

                diffs.add(Diff.generateDiff(Operation.REPLACE, path, target));
            }
        }
    }

    private static void compareArray(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        List<JsonNode> lcs = getLCS(source, target);
        int srcIdx = 0;
        int targetIdx = 0;
        int lcsIdx = 0;
        int srcSize = source.size();
        int targetSize = target.size();
        int lcsSize = lcs.size();

        int pos = 0;
        while (lcsIdx < lcsSize) {
            JsonNode lcsNode = lcs.get(lcsIdx);
            JsonNode srcNode = source.get(srcIdx);
            JsonNode targetNode = target.get(targetIdx);


            if (lcsNode.equals(srcNode) && lcsNode.equals(targetNode)) { // Both are same as lcs node, nothing to do here
                srcIdx++;
                targetIdx++;
                lcsIdx++;
                pos++;
            } else {
                if (lcsNode.equals(srcNode)) { // src node is same as lcs, but not targetNode
                    //addition
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
                    pos++;
                    targetIdx++;
                } else if (lcsNode.equals(targetNode)) { //targetNode node is same as lcs, but not src
                    //removal,
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, srcNode));
                    srcIdx++;
                } else {
                    List<Object> currPath = getPath(path, pos);
                    //both are unequal to lcs node
                    generateDiffs(diffs, currPath, srcNode, targetNode);
                    srcIdx++;
                    targetIdx++;
                    pos++;
                }
            }
        }

        while ((srcIdx < srcSize) && (targetIdx < targetSize)) {
            JsonNode srcNode = source.get(srcIdx);
            JsonNode targetNode = target.get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            generateDiffs(diffs, currPath, srcNode, targetNode);
            srcIdx++;
            targetIdx++;
            pos++;
        }
        pos = addRemaining(diffs, path, target, pos, targetIdx, targetSize);
        removeRemaining(diffs, path, pos, srcIdx, srcSize, source);
    }

    private static Integer removeRemaining(List<Diff> diffs, List<Object> path, int pos, int srcIdx, int srcSize, JsonNode source) {

        while (srcIdx < srcSize) {
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(srcIdx)));
            srcIdx++;
        }
        return pos;
    }

    private static Integer addRemaining(List<Diff> diffs, List<Object> path, JsonNode target, int pos, int targetIdx, int targetSize) {
        while (targetIdx < targetSize) {
            JsonNode jsonNode = target.get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, jsonNode.deepCopy()));
            pos++;
            targetIdx++;
        }
        return pos;
    }

    private static void compareObjects(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        Iterator<String> keysFromSrc = source.fieldNames();
        while (keysFromSrc.hasNext()) {
            String key = keysFromSrc.next();
            if (!target.has(key)) {
                //remove case
                List<Object> currPath = getPath(path, key);
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(key)));
                continue;
            }
            List<Object> currPath = getPath(path, key);
            generateDiffs(diffs, currPath, source.get(key), target.get(key));
        }
        Iterator<String> keysFromTarget = target.fieldNames();
        while (keysFromTarget.hasNext()) {
            String key = keysFromTarget.next();
            if (!source.has(key)) {
                //add case
                List<Object> currPath = getPath(path, key);
                diffs.add(Diff.generateDiff(Operation.ADD, currPath, target.get(key)));
            }
        }
    }

    private static List<Object> getPath(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>();
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    private static List<JsonNode> getLCS(final JsonNode first, final JsonNode second) {

        Preconditions.checkArgument(first.isArray(), "LCS can only work on JSON arrays");
        Preconditions.checkArgument(second.isArray(), "LCS can only work on JSON arrays");

        return ListUtils.longestCommonSubsequence(Lists.newArrayList(first), Lists.newArrayList(second));
    }
}
