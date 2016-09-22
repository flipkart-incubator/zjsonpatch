package com.flipkart.zjsonpatch;

import java.util.ArrayList;
import java.util.List;

public class CompactHelper {

    public static void compact(final List<Diff> diffs) {
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

    // Note : only to be used for arrays
    // Finds the longest common Ancestor ending at Array
    private static void computeRelativePath(List<Object> path, int startIdx, int endIdx, List<Diff> diffs) {
        List<Integer> counters = new ArrayList<Integer>();

        resetCounters(counters, path.size());

        for (int i = startIdx; i <= endIdx; i++) {
            Diff diff = diffs.get(i);
            // Adjust relative path according to #Add and #Remove
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
        // find longest common prefix of both the paths
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
}
