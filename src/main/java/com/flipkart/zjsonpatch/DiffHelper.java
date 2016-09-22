package com.flipkart.zjsonpatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.functors.DefaultEquator;
import org.apache.commons.collections4.sequence.CommandVisitor;
import org.apache.commons.collections4.sequence.SequencesComparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

class DiffHelper {

    private static final class LcsDiffVisitor implements CommandVisitor<JsonNode> {

        private List<Diff> diffs;
        private List<Object> path;
        private int pos;

        public LcsDiffVisitor(List<Diff> diffs, List<Object> path, int start) {
            this.diffs = diffs;
            this.path = path;
            this.pos = start;
        }

        @Override
        public void visitInsertCommand(JsonNode object) {
            List<Object> currPath = JsonPathHelper.getPathExt(path, pos++);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, object));
        }

        @Override
        public void visitKeepCommand(JsonNode object) {
            pos++;
        }

        @Override
        public void visitDeleteCommand(JsonNode object) {
            List<Object> currPath = JsonPathHelper.getPathExt(path, pos);
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, object));
        }
    }

    public static List<Diff> create(JsonNode source, JsonNode target) {
        return generateDiffs(new ArrayList<Diff>(), new ArrayList<Object>(), source, target);
    }

    private static List<Diff> generateDiffs(final List<Diff> diffs, final List<Object> path, JsonNode source,
            JsonNode target) {
        if (!source.equals(target)) {
            JsonNodeType sourceType = source.getNodeType();
            JsonNodeType targetType = target.getNodeType();

            if (sourceType == JsonNodeType.ARRAY && targetType == JsonNodeType.ARRAY) {
                compareArray(diffs, path, source, target);
            } else if (sourceType == JsonNodeType.OBJECT && targetType == JsonNodeType.OBJECT) {
                compareObjects(diffs, path, source, target);
            } else {
                diffs.add(Diff.generateDiff(Operation.REPLACE, path, target));
            }
        }
        return diffs;
    }

    private static void compareArray(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        compareArray(diffs, path, newArrayList(source), newArrayList(target), 0);
    }

    private static void compareArray(List<Diff> diffs, List<Object> path, List<JsonNode> source, List<JsonNode> target,
            int start) {
        int srcEnd = start + source.size();
        int targetEnd = start + target.size();
        while ((start < srcEnd) && (start < targetEnd)) {
            if (!compareArrayEquals(source, start, target, start)) {
                break;
            }
            start++;
        }
        while ((start < srcEnd) && (start < targetEnd)) {
            if (!compareArrayEquals(source, --srcEnd, target, --targetEnd)) {
                srcEnd++;
                targetEnd++;
                break;
            }
        }
        compareArrayLcs(diffs, path, source.subList(start, srcEnd), target.subList(start, targetEnd), start);
    }

    private static boolean compareArrayEquals(List<JsonNode> source, int sindex, List<JsonNode> target, int tindex) {
        return source.get(sindex).equals(target.get(tindex));
    }

    private static void compareArrayLcs(List<Diff> diffs, List<Object> path, List<JsonNode> source,
            List<JsonNode> target, int start) {
        SequencesComparator<JsonNode> comparator =
                new SequencesComparator<JsonNode>(source, target, DefaultEquator.defaultEquator());
        comparator.getScript().visit(new LcsDiffVisitor(diffs, path, start));
    }

    private static void compareObjects(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        Iterator<String> keysFromSrc = source.fieldNames();
        while (keysFromSrc.hasNext()) {
            String key = keysFromSrc.next();
            if (!target.has(key)) { // remove case
                List<Object> currPath = JsonPathHelper.getPathExt(path, key);
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(key)));
                continue;
            }
            List<Object> currPath = JsonPathHelper.getPathExt(path, key);
            generateDiffs(diffs, currPath, source.get(key), target.get(key));
        }
        Iterator<String> keysFromTarget = target.fieldNames();
        while (keysFromTarget.hasNext()) {
            String key = keysFromTarget.next();
            if (!source.has(key)) { // add case
                List<Object> currPath = JsonPathHelper.getPathExt(path, key);
                diffs.add(Diff.generateDiff(Operation.ADD, currPath, target.get(key)));
            }
        }
    }

    private static List<JsonNode> newArrayList(JsonNode node) {
        List<JsonNode> list = new ArrayList<JsonNode>();
        for (JsonNode elem : node) {
            list.add(elem);
        }
        return list;
    }
}
