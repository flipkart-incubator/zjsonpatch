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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */

public class JsonDiff {

    private Map<String, List<String>> pathToLabelFields = null;

    public JsonDiff() {

    }

    /**
     * The path-key will be used as a regular expression to match paths in the diff in order to add labels for the
     * associated fields.<br>
     * <br>
     * Paths should be forward slash separated.<br>
     * <pre>
     * {@literal Map<String, List<String>> pathToLabelFields= new MapBuilder<String, List<String>>()
     *                 .put("/able/baker/\\[.*?\\]", Arrays.asList("selectedBy"))
     *                 .put("/charlie/dog/\\d+", Arrays.asList("selectedBy"))
     *                 .put("/easy/fox/\\[.*?\\]", Arrays.asList("selectedBy"))
     *                 .put("/george/how/\\d+", Arrays.asList("selectedBy"))
     *                 .build();}
     * </pre>
     * @param pathToLabelFields
     * @param <T>
     * @return
     */
    public <T extends JsonDiff> T withPathToLabelFields(Map<String, List<String>> pathToLabelFields) {
        this.pathToLabelFields = pathToLabelFields;
        return (T)this;
    }

    /**
     * The path-key will be used as a regular expression to match paths in the diff.
     *
     * @param pathToLabelFields
     */
    public void setPathToLabelFields(Map<String, List<String>> pathToLabelFields) {
        this.pathToLabelFields = pathToLabelFields;
    }

    public Map<String, List<String>> getPathToLabelFields() {
        return pathToLabelFields;
    }

    public JsonNode asJson(JsonDiffParams params) {
        return asJson(params.source, params.target, params.flags, params.unimportantPatterns, params.arrayKeyMap, params.om);
    }

    private JsonNode asJson(final JsonNode source, final JsonNode target, EnumSet<DiffFlags> flags, List<String> unimportantPatterns, Map<String, List<String>> arrayKeyMap, ObjectMapper om) {
        final List<Diff> diffs = new ArrayList<Diff>();
        List<Object> path = new ArrayList<Object>(0);

        if (flags.contains(DiffFlags.IGNORE_ID)) {

            // Strip $id values from json structure

            InternalUtils.stripIds(source);
            InternalUtils.stripIds(target);
        }

        if (flags.contains(DiffFlags.OBJECTIFY_ARRAYS)) {

            // Turn arrays into objects based on provided keyMap

            if (null == arrayKeyMap || arrayKeyMap.isEmpty()) {
                throw new IllegalArgumentException("Invalid flag: " + DiffFlags.OBJECTIFY_ARRAYS + ", used with null or empty key map.");
            }

            // Add a metadata entry to the diff to allow patch to be applied
            diffs.add(Diff.generateDiff(Operation.METADATA, Collections.singletonList(DiffFlags.OBJECTIFY_ARRAYS), om.valueToTree(arrayKeyMap)));

            InternalUtils.objectifyArrays(source, arrayKeyMap);
            InternalUtils.objectifyArrays(target, arrayKeyMap);
        }

        // generating diffs in the order of their occurrence

        generateDiffs(diffs, path, source, target);

        if (!flags.contains(DiffFlags.OMIT_MOVE_OPERATION)) {

            // Merging remove & add to move operation

            compactDiffs(diffs);
        }

        if (!flags.contains(DiffFlags.OMIT_COPY_OPERATION)) {

            // Introduce copy operation

            introduceCopyOperation(source, target, diffs);
        }

        if (!flags.contains(DiffFlags.INCLUDE_LABELS_OPERATION)) {

            // Remove labels

            removeLabels(diffs);
        }

        return getJsonNodes(diffs, flags, unimportantPatterns, om);
    }

    private List<Object> getMatchingValuePath(Map<JsonNode, List<Object>> unchangedValues, JsonNode value) {
        return unchangedValues.get(value);
    }

    private void introduceCopyOperation(JsonNode source, JsonNode target, List<Diff> diffs) {
        Map<JsonNode, List<Object>> unchangedValues = getUnchangedPart(source, target);
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff = diffs.get(i);
            if (Operation.ADD == diff.getOperation()) {
                List<Object> matchingValuePath = getMatchingValuePath(unchangedValues, diff.getValue());
                if (matchingValuePath != null && isAllowed(matchingValuePath, diff.getPath())) {
                    diffs.set(i, new Diff(Operation.COPY, matchingValuePath, diff.getPath()));
                }
            }
        }
    }

    private void removeLabels(List<Diff> diffs) {
        for (Iterator<Diff> i = diffs.listIterator(); i.hasNext(); ) {
            Diff diff = i.next();
            if (Operation.LABEL == diff.getOperation()) {
                i.remove();
            }
        }
    }

    private boolean isNumber(String str) {
        int size = str.length();

        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return size > 0;
    }

    private boolean isAllowed(List<Object> source, List<Object> destination) {
        boolean isSame = source.equals(destination);
        int i = 0;
        int j = 0;
        //Hack to fix broken COPY operation, need better handling here
        while (i < source.size() && j < destination.size()) {
            Object srcValue = source.get(i);
            Object dstValue = destination.get(j);
            String srcStr = srcValue.toString();
            String dstStr = dstValue.toString();
            if (isNumber(srcStr) && isNumber(dstStr)) {

                if (srcStr.compareTo(dstStr) > 0) {
                    return false;
                }
            }
            i++;
            j++;

        }
        return !isSame;
    }

    private Map<JsonNode, List<Object>> getUnchangedPart(JsonNode source, JsonNode target) {
        Map<JsonNode, List<Object>> unchangedValues = new HashMap<JsonNode, List<Object>>();
        computeUnchangedValues(unchangedValues, new ArrayList<Object>(), source, target);
        return unchangedValues;
    }

    private void computeUnchangedValues(Map<JsonNode, List<Object>> unchangedValues, List<Object> path, JsonNode source, JsonNode target) {
        if (source.equals(target)) {
            if (!unchangedValues.containsKey(target)) {
                unchangedValues.put(target, path);
            }
            return;
        }

        final NodeType firstType = NodeType.getNodeType(source);
        final NodeType secondType = NodeType.getNodeType(target);

        if (firstType == secondType) {
            switch (firstType) {
                case OBJECT:
                    computeObject(unchangedValues, path, source, target);
                    break;
                case ARRAY:
                    computeArray(unchangedValues, path, source, target);
                    break;
                default:
                    /* nothing */
            }
        }
    }

    private void computeArray(Map<JsonNode, List<Object>> unchangedValues, List<Object> path, JsonNode source, JsonNode target) {
        final int size = Math.min(source.size(), target.size());

        for (int i = 0; i < size; i++) {
            List<Object> currPath = getPath(path, i);
            computeUnchangedValues(unchangedValues, currPath, source.get(i), target.get(i));
        }
    }

    private void computeObject(Map<JsonNode, List<Object>> unchangedValues, List<Object> path, JsonNode source, JsonNode target) {
        final Iterator<String> firstFields = source.fieldNames();
        while (firstFields.hasNext()) {
            String name = firstFields.next();
            if (target.has(name)) {
                List<Object> currPath = getPath(path, name);
                computeUnchangedValues(unchangedValues, currPath, source.get(name), target.get(name));
            }
        }
    }

    /**
     * This method merge 2 diffs ( remove then add, or vice versa ) with same value into one Move operation,
     * all the core logic resides here only
     */
    private void compactDiffs(List<Diff> diffs) {
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff1 = diffs.get(i);

            // if not remove OR add, move to next diff
            if (!(Operation.REMOVE == diff1.getOperation() ||
                    Operation.ADD == diff1.getOperation())) {
                continue;
            }

            for (int j = i + 1; j < diffs.size(); j++) {
                Diff diff2 = diffs.get(j);
                if (!diff1.getValue().equals(diff2.getValue())) {
                    continue;
                }

                Diff moveDiff = null;
                if (Operation.REMOVE == diff1.getOperation() &&
                        Operation.ADD == diff2.getOperation()) {
                    computeRelativePath(diff2.getPath(), i + 1, j - 1, diffs);
                    moveDiff = new Diff(Operation.MOVE, diff1.getPath(), diff2.getPath());

                } else if (Operation.ADD == diff1.getOperation() &&
                        Operation.REMOVE == diff2.getOperation()) {
                    computeRelativePath(diff2.getPath(), i, j - 1, diffs); // diff1's add should also be considered
                    moveDiff = new Diff(Operation.MOVE, diff2.getPath(), diff1.getPath());
                }
                if (moveDiff != null) {
                    diffs.remove(j);
                    diffs.set(i, moveDiff);
                    break;
                }
            }
        }
    }

    //Note : only to be used for arrays
    //Finds the longest common Ancestor ending at Array
    private void computeRelativePath(List<Object> path, int startIdx, int endIdx, List<Diff> diffs) {
        List<Integer> counters = new ArrayList<Integer>(path.size());

        resetCounters(counters, path.size());

        for (int i = startIdx; i <= endIdx; i++) {
            Diff diff = diffs.get(i);
            //Adjust relative path according to #ADD and #Remove
            if (Operation.ADD == diff.getOperation() || Operation.REMOVE == diff.getOperation()) {
                updatePath(path, diff, counters);
            }
        }
        updatePathWithCounters(counters, path);
    }

    private void resetCounters(List<Integer> counters, int size) {
        for (int i = 0; i < size; i++) {
            counters.add(0);
        }
    }

    private void updatePathWithCounters(List<Integer> counters, List<Object> path) {
        for (int i = 0; i < counters.size(); i++) {
            int value = counters.get(i);
            if (value != 0) {
                Integer currValue = Integer.parseInt(path.get(i).toString());
                path.set(i, String.valueOf(currValue + value));
            }
        }
    }

    private void updatePath(List<Object> path, Diff pseudo, List<Integer> counters) {
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

    private void updateCounters(Diff pseudo, int idx, List<Integer> counters) {
        if (Operation.ADD == pseudo.getOperation()) {
            counters.set(idx, counters.get(idx) - 1);
        } else {
            if (Operation.REMOVE == pseudo.getOperation()) {
                counters.set(idx, counters.get(idx) + 1);
            }
        }
    }

    private ArrayNode getJsonNodes(List<Diff> diffs, EnumSet<DiffFlags> flags, List<String> unimportantPatterns, ObjectMapper om) {
        JsonNodeFactory FACTORY = JsonNodeFactory.instance;
        final ArrayNode patch = FACTORY.arrayNode();
        for (Diff diff : diffs) {
            ObjectNode jsonNode = getJsonNode(FACTORY, diff, flags, unimportantPatterns, om);
            patch.add(jsonNode);
        }
        return patch;
    }

    private ObjectNode getJsonNode(JsonNodeFactory FACTORY, Diff diff, EnumSet<DiffFlags> flags, List<String> unimportantPatterns, ObjectMapper om) {
        ObjectNode jsonNode = FACTORY.objectNode();
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

            case LABEL:
                // Treat LABEL operations as REPLACE for output.
                jsonNode.put(Constants.OP, Operation.REPLACE.rfcName());
                // LABEL operations are automatically unimportant
                if (flags.contains(DiffFlags.INCLUDE_UNIMPORTANT_CHANGES)) {
                    jsonNode.put(Constants.UNIMPORTANT, true);
                }
            case REPLACE:
                if (flags.contains(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE)) {
                    jsonNode.set(Constants.FROM_VALUE, diff.getSrcValue());
                }
                if (diff.getOperation().equals(Operation.REPLACE) && // This process is unique to REPLACE, and shouldn't fall through for LABEL
                        flags.contains(DiffFlags.INCLUDE_UNIMPORTANT_CHANGES)) {
                    // Check regular expressions
                    boolean matches = false;
                    for (String pattern : unimportantPatterns) {
                        matches |= (diff.getSrcValue().asText().matches(pattern) && diff.getValue().asText().matches(pattern));
                    }
                    if (matches) {
                        jsonNode.put(Constants.UNIMPORTANT, true);
                    } else {
                        jsonNode.put(Constants.UNIMPORTANT, false);
                    }
                }
            case ADD:
            case TEST:
                jsonNode.put(Constants.PATH, PathUtils.getPathRepresentation(diff.getPath()));
                jsonNode.set(Constants.VALUE, diff.getValue());
                break;
            case METADATA:
                // Serialize value as text
                String val;
                try {
                    val = om.writeValueAsString(diff.getValue());
                } catch (JsonProcessingException jpe) {
                    throw new RuntimeException("Failed serialize json: " + diff.getValue());
                }

                jsonNode.put(Constants.PATH, diff.getPath().stream().map(Object::toString).collect(Collectors.joining("")));
                jsonNode.put(Constants.VALUE, val);
                break;

            default:
                // Safety net
                throw new IllegalArgumentException("Unknown operation specified: " + diff.getOperation());
        }

        return jsonNode;
    }

    protected boolean generateDiffs(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
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

                diffs.add(Diff.generateDiff(Operation.REPLACE, path, source, target));
                return true;
            }
        }
        return false;
    }

    protected void compareArray(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
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

    protected void compareObjects(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        Iterator<String> keysFromSrc = source.fieldNames();
        boolean hasChange = false;
        while (keysFromSrc.hasNext()) {
            String key = keysFromSrc.next();
            if (!target.has(key)) {
                //remove case
                List<Object> currPath = getPath(path, key);
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(key)));
                continue;
            }
            List<Object> currPath = getPath(path, key);
            boolean result = generateDiffs(diffs, currPath, source.get(key), target.get(key));
            hasChange = hasChange || result;
        }
        if (hasChange) { // If the child has a change a label operation should be added
            addLabelDiff(diffs, path, source, target);
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

    /**
     * Add any desired {@link Operation#LABEL} Diff nodes to the <code>diffs</code> list for the given
     * <code>path</code>, <code>source</code>, <code>target</code>.
     *
     * By default, it adds the "name" field and any matching pathToLabelFields values for the path.  Extending classes can call <code>super.addLabelDiff</code> to include
     * this without rewriting as "name" fields are often desired.
     *
     * @param diffs
     * @param path
     * @param source
     * @param target
     */
    protected void addLabelDiff(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        // Always include the name field
        appendFieldLabel(diffs, path, source, target, "name");

        if (null != pathToLabelFields) {
            // Java8 has streams and joiners, this source is Java6 though.
            // The paths are expected to have forward slash ("/") for the separator.
            StringBuilder slashedPath = new StringBuilder();
            for (Object o : path) {
                slashedPath.append("/").append(o);
            }
            final List<String> fields = findPathToLabelFields(slashedPath.toString());
            if (null != fields && !fields.isEmpty()) {
                for(String field: fields) {
                    // We already did "name" above.
                    if (!"name".equals(field)) {
                        appendFieldLabel(diffs, path, source, target, field);
                    }
                }
            }
        }
    }

    /**
     * Find the first matching List of field names for the given path.
     *
     * @param slashedPath
     * @return
     */
    private List<String> findPathToLabelFields(String slashedPath) {
        if (null == pathToLabelFields) {
            return Collections.emptyList();
        }
        for(Map.Entry<String, List<String>> entry: pathToLabelFields.entrySet()) {
            if (Pattern.matches(entry.getKey(), slashedPath)) {
                return entry.getValue();
            }
        }
        return null;
    }
    private void appendFieldLabel(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target, String fieldName) {
        JsonNode srcField = source.get(fieldName);
        JsonNode tarField = target.get(fieldName);
        if (srcField != null && !srcField.asText().equals("") && tarField != null && !tarField.asText().equals("")) {
            List<Object> fieldPath = getPath(path, fieldName);
            Diff tmpDiff = Diff.generateDiff(Operation.LABEL, fieldPath, srcField, tarField);
            diffs.add(tmpDiff);
        }
    }

    private List<Object> getPath(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>(path.size() + 1);
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    private List<JsonNode> getLCS(final JsonNode first, final JsonNode second) {
        return ListUtils.longestCommonSubsequence(InternalUtils.toList((ArrayNode) first), InternalUtils.toList((ArrayNode) second));
    }

    public static class JsonDiffParams {
        final JsonNode source;
        final JsonNode target;
        EnumSet<DiffFlags> flags;
        List<String> unimportantPatterns;
        Map<String, List<String>> arrayKeyMap;
        ObjectMapper om;

        public JsonDiffParams(JsonNode source, JsonNode target) {
            this.source = source;
            this.target = target;
            this.flags = DiffFlags.defaults();
            this.unimportantPatterns = new ArrayList<>();
            this.arrayKeyMap = new HashMap<>();
            this.om = new ObjectMapper();
        }
        public JsonDiffParams flags(EnumSet<DiffFlags> flags) {
            this.flags = flags;
            return this;
        }
        public JsonDiffParams unimportantPatterns(List<String> unimportantPatterns) {
            this.unimportantPatterns = unimportantPatterns;
            return this;
        }
        public JsonDiffParams arrayKeyMap(Map<String, List<String>> arrayKeyMap) {
            this.arrayKeyMap = arrayKeyMap;
            return this;
        }
        public JsonDiffParams om(ObjectMapper om) {
            this.om = om;
            return this;
        }
    }
}
