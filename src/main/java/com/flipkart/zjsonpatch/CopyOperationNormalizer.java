package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CopyOperationNormalizer extends AbstractOperationNormalizer {

    CopyOperationNormalizer() {
        super();
    }

    CopyOperationNormalizer(ComparisonCriteria criteria) {
        super(criteria);
    }

    @Override
    public void normalize(JsonNode source, JsonNode target, List<Diff> diffs) {
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

    private Map<JsonNode, List<Object>> getUnchangedPart(JsonNode source, JsonNode target) {
        Map<JsonNode, List<Object>> unchangedValues = new HashMap<JsonNode, List<Object>>();
        computeUnchangedValues(unchangedValues, new ArrayList<Object>(), source, target);
        return unchangedValues;
    }

    private List<Object> getMatchingValuePath(Map<JsonNode, List<Object>> unchangedValues, JsonNode value) {
        return unchangedValues.get(value);
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

    private boolean isNumber(String str) {
        int size = str.length();

        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return size > 0;
    }
}
