package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.functors.DefaultEquator;
import org.apache.commons.collections4.sequence.CommandVisitor;
import org.apache.commons.collections4.sequence.SequencesComparator;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
public final class JsonDiff {

    private JsonDiff() {}

    private static final class LcsDiffVisitor  implements CommandVisitor<JsonNode> {
    
        private List<Diff> diffs;
        private List<Object> path;
        private int pos, size;
    
        public LcsDiffVisitor(List<Diff> diffs, List<Object> path, int start, int size) {
            this.diffs = diffs;
            this.path = path;
            this.pos = start;
            this.size = size;
        }
    
        @Override
        public void visitInsertCommand(JsonNode object) {
            List<Object> currPath = getPath(path, pos /*pos >= size ? "-" : pos*/);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, object));
            pos++; size++;
        }
    
        @Override
        public void visitKeepCommand(JsonNode object) {
            pos++;
        }
    
        @Override
        public void visitDeleteCommand(JsonNode object) {
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, object));
            size--;
        }
    }

    public static JsonNode asJson(final JsonNode source, final JsonNode target) {
        return asJson(source, target, CompatibilityFlags.defaults());
    }

    public static JsonNode asJson(final JsonNode source, final JsonNode target, final Set<CompatibilityFlags> flags) {
        final List<Diff> diffs = new ArrayList<Diff>();
        final List<Object> path = new ArrayList<Object>();
        /**
         * generating diffs in the order of their occurrence
         */
        generateDiffs(diffs, path, source, target);
        /**
         * Merging remove & add to move operation
         */
        if (!flags.contains(CompatibilityFlags.DISABLE_DIFF_OPTIMIZATION)) {
            compactDiffs(diffs);
        }

        return getJsonNodes(diffs);
    }

    /**
     * This method merge 2 diffs ( remove then add, or vice versa ) with same value into one Move operation,
     * all the core logic resides here only
     */
    private static void compactDiffs(List<Diff> diffs) {
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff1 = diffs.get(i);

            // if not remove OR add, move to next diff
            if (!(Operation.REMOVE.equals(diff1.getOperation()) ||
                    Operation.ADD.equals(diff1.getOperation()))) {
                continue;
            }

            for (int j = i + 1; j < diffs.size(); j++) {
                Diff diff2 = diffs.get(j);
                if (!diff1.getValue().equals(diff2.getValue())) {
                    continue;
                }

                Diff moveDiff = null;
                if (Operation.REMOVE.equals(diff1.getOperation()) &&
                        Operation.ADD.equals(diff2.getOperation())) {
                    computeRelativePath(diff2.getPath(), i + 1, j - 1, diffs);
                    moveDiff = new Diff(Operation.MOVE, diff1.getPath(), diff2.getValue(), diff2.getPath());

                } else if (Operation.ADD.equals(diff1.getOperation()) &&
                        Operation.REMOVE.equals(diff2.getOperation())) {
                    computeRelativePath(diff2.getPath(), i, j - 1, diffs); // diff1's add should also be considered
                    moveDiff = new Diff(Operation.MOVE, diff2.getPath(), diff1.getValue(), diff1.getPath());
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
        jsonNode.put(Constants.OP, diff.getOperation().rfcName());
        jsonNode.put(Constants.PATH, getArrayNodeRepresentation(diff.getPath()));
        if (Operation.MOVE.equals(diff.getOperation())) {
            jsonNode.put(Constants.FROM, getArrayNodeRepresentation(diff.getPath())); //required {from} only in case of Move Operation
            jsonNode.put(Constants.PATH, getArrayNodeRepresentation(diff.getToPath()));  // destination Path
        }
        if (!Operation.REMOVE.equals(diff.getOperation()) && !Operation.MOVE.equals(diff.getOperation())) { // setting only for Non-Remove operation
            jsonNode.put(Constants.VALUE, diff.getValue());
        }
        return jsonNode;
    }

    private static String getArrayNodeRepresentation(List<Object> path) {
        StringBuilder builder = new StringBuilder();
        for (Object elem : path) {
            builder.append('/').append(encodeSubPath(elem.toString()));
        }
        return builder.toString();
    }

    private static String encodeSubPath(String path) {
        // see http://tools.ietf.org/html/rfc6901#section-4
        return path.replaceAll("~", "~0").replaceAll("/", "~1");
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
        compareArray(diffs, path, newArrayList(source), newArrayList(target), 0);
    }

    private static List<JsonNode> newArrayList(JsonNode node) {
        List<JsonNode> list = new ArrayList<JsonNode>();
        for (JsonNode elem : node) {
            list.add(elem);
        }
        return list;
    }

    private static void compareArray(List<Diff> diffs, List<Object> path, List<JsonNode> source, List<JsonNode> target,
            int start) {
        int srcEnd = start + source.size();
        int targetEnd = start + target.size();
        while ((start < srcEnd) && (start < targetEnd)) {
            if (!equals(source, start, target, start)) {
                break;
            }
            start++;
        }
        while ((start < srcEnd) && (start < targetEnd)) {
            if (!equals(source, --srcEnd, target, --targetEnd)) {
                srcEnd++; targetEnd++;
                break;
            }
        }
        compareArrayLcs(diffs, path, source.subList(start, srcEnd), target.subList(start, targetEnd), start);
    }

    private static void compareArrayLcs(List<Diff> diffs, List<Object> path, List<JsonNode> source,
            List<JsonNode> target, int start) {
        SequencesComparator<JsonNode> comparator =
                new SequencesComparator<JsonNode>(source, target, DefaultEquator.defaultEquator());
        comparator.getScript().visit(new LcsDiffVisitor(diffs, path, start, source.size()));
    }

    private static boolean equals(List<JsonNode> source, int sindex, List<JsonNode> target, int tindex) {
        return source.get(sindex).equals(target.get(tindex));
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
}
