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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.ListUtils;

import java.util.*;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */

public final class JsonDiff {

    /**
     * Same method as {@link JsonDiff#asJson(JsonNode, JsonNode, EnumSet)} but
     * uses a default internal instance for performance.
     *
     * @see JsonDiff#asJson(JsonNode, JsonNode, EnumSet)
     */
    public static JsonNode asJson(final JsonNode source, final JsonNode target) {
        return DEFAULT.createDiffPatch(source, target);
    }

    /**
     * Same method as {@link JsonDiff#createDiffPatch(JsonNode, JsonNode)}, but creates
     * a one time use instance of {@link JsonDiff} with the given {@code flags}.
     *
     * @param flags a collection of {@link DiffFlags} used to control the behavior of the patch generation.
     * @see JsonDiff#createDiffPatch(JsonNode, JsonNode
     */
    public static JsonNode asJson(final JsonNode source, final JsonNode target, EnumSet<DiffFlags> flags) {
        JsonDiff jsonDiff = new JsonDiff(ComparisonCriteria.DEFAULT_CRITERIA, flags);
        return jsonDiff.createDiffPatch(source, target);
    }

    private static final JsonDiff DEFAULT = new JsonDiff();

    /**
     * List of normalizers that will be used with this {@link JsonDiff}.
     */
    private final List<OperationNormalizer> normalizers = new ArrayList<OperationNormalizer>();

    /**
     * The {@link ComparisonCriteria} that will be used to determine {@link JsonNode} equality in this {@link JsonDiff}.
     */
    private final ComparisonCriteria criteria;

    /**
     * The {@link DiffFlags} used to build this {@link JsonDiff}.
     */
    private final EnumSet<DiffFlags> flags;

    private JsonDiff() {
        this(ComparisonCriteria.DEFAULT_CRITERIA, DiffFlags.defaults());
    }

    private JsonDiff(ComparisonCriteria criteria, Set<DiffFlags> flags) {
        this.criteria = criteria;
        this.flags = EnumSet.copyOf(flags);

        configureFlags();
    }

    private void configureFlags() {
        if (!flags.contains(DiffFlags.OMIT_MOVE_OPERATION)) {
            normalizers.add(new MoveOperationNormalizer(this.criteria));
        }

        if (!flags.contains(DiffFlags.OMIT_COPY_OPERATION)) {
            normalizers.add(new CopyOperationNormalizer(this.criteria));
        }
    }

    /**
     * Generate a RFC 6902 compliant {@link JsonNode} containing a set of operations (patch)
     * that takes the {@code source} and calculate the steps to transform it in the {@code target}.
     * Note that the patch will generate the differences in order of occurrence.
     *
     * @param source the {@link JsonNode} used as initial state of the creation of the patch.
     * @param target the {@link JsonNode} used as final state of the creation of the patch.
     * @return a RFC 6902 compliant {@link JsonNode} object containing a set of operations with the
     * differences between {@code source} and {@code target}.
     * @see DiffFlags
     */
    public JsonNode createDiffPatch(final JsonNode source, final JsonNode target) {
        final List<Diff> diffs = new ArrayList<Diff>();
        List<Object> path = new ArrayList<Object>(0);

        generateDiffs(diffs, path, source, target);
        for (OperationNormalizer normalizer : normalizers) {
            normalizer.normalize(source, target, diffs);
        }
        return getJsonNodes(diffs, flags);
    }

    private static ArrayNode getJsonNodes(List<Diff> diffs, EnumSet<DiffFlags> flags) {
        JsonNodeFactory FACTORY = JsonNodeFactory.instance;
        final ArrayNode patch = FACTORY.arrayNode();
        for (Diff diff : diffs) {
            ObjectNode jsonNode = getJsonNode(FACTORY, diff, flags);
            patch.add(jsonNode);
        }
        return patch;
    }

    private static ObjectNode getJsonNode(JsonNodeFactory factory, Diff diff, EnumSet<DiffFlags> flags) {
        ObjectNode jsonNode = factory.objectNode();
        jsonNode.put(Constants.OP, diff.getOperation().rfcName());

        switch (diff.getOperation()) {
            case MOVE:
            case COPY:
                jsonNode.put(Constants.FROM, PathUtils.getPathRepresentation(diff.getPath()));    // required {from} only in case of Move Operation
                jsonNode.put(Constants.PATH, PathUtils.getPathRepresentation(diff.getToPath()));  // destination Path
                break;

            case REMOVE:
                jsonNode.put(Constants.PATH, PathUtils.getPathRepresentation(diff.getPath()));
                if (!flags.contains(DiffFlags.OMIT_VALUE_ON_REMOVE))
                    jsonNode.set(Constants.VALUE, diff.getValue());
                break;

            case REPLACE:
                if (flags.contains(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE)) {
                    jsonNode.set(Constants.FROM_VALUE, diff.getSrcValue());
                }
            case ADD:
            case TEST:
                jsonNode.put(Constants.PATH, PathUtils.getPathRepresentation(diff.getPath()));
                jsonNode.set(Constants.VALUE, diff.getValue());
                break;

            default:
                // Safety net
                throw new IllegalArgumentException("Unknown operation specified:" + diff.getOperation());
        }

        return jsonNode;
    }

    private void generateDiffs(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        if (!source.equals(criteria, target)) {
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

                diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
            }
        }
    }

    private void compareArray(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
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


            if (lcsNode.equals(criteria, srcNode) && lcsNode.equals(criteria, targetNode)) { // Both are same as lcs node, nothing to do here
                srcIdx++;
                targetIdx++;
                lcsIdx++;
                pos++;
            } else {
                if (lcsNode.equals(criteria, srcNode)) { // src node is same as lcs, but not targetNode
                    //addition
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
                    pos++;
                    targetIdx++;
                } else if (lcsNode.equals(criteria, targetNode)) { //targetNode node is same as lcs, but not src
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

    private Integer removeRemaining(List<Diff> diffs, List<Object> path, int pos, int srcIdx, int srcSize, JsonNode source) {

        while (srcIdx < srcSize) {
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(srcIdx)));
            srcIdx++;
        }
        return pos;
    }

    private Integer addRemaining(List<Diff> diffs, List<Object> path, JsonNode target, int pos, int targetIdx, int targetSize) {
        while (targetIdx < targetSize) {
            JsonNode jsonNode = target.get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, jsonNode.deepCopy()));
            pos++;
            targetIdx++;
        }
        return pos;
    }

    private void compareObjects(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
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

    private List<Object> getPath(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>(path.size() + 1);
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    private static List<JsonNode> getLCS(final JsonNode first, final JsonNode second) {
        return ListUtils.longestCommonSubsequence(InternalUtils.toList((ArrayNode) first), InternalUtils.toList((ArrayNode) second));
    }
}