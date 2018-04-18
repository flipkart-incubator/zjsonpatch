package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class AbstractOperationNormalizer implements OperationNormalizer {
    protected final ComparisonCriteria criteria;

    AbstractOperationNormalizer() {
        this(ComparisonCriteria.DEFAULT_CRITERIA);
    }

    AbstractOperationNormalizer(ComparisonCriteria criteria) {
        this.criteria = criteria;
    }

    private static List<Object> getPath(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>(path.size() + 1);
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    void computeUnchangedValues(
            Map<JsonNode, List<Object>> unchangedValues,
            List<Object> path,
            JsonNode source,
            JsonNode target
    ) {
        if (source.equals(criteria, target)) {
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

    void computeArray(
            Map<JsonNode, List<Object>> unchangedValues,
            List<Object> path,
            JsonNode source,
            JsonNode target
    ) {
        final int size = Math.min(source.size(), target.size());

        for (int i = 0; i < size; i++) {
            List<Object> currPath = getPath(path, i);
            computeUnchangedValues(unchangedValues, currPath, source.get(i), target.get(i));
        }
    }

    void computeObject(
            Map<JsonNode, List<Object>> unchangedValues,
            List<Object> path,
            JsonNode source,
            JsonNode target
    ) {
        final Iterator<String> firstFields = source.fieldNames();
        while (firstFields.hasNext()) {
            String name = firstFields.next();
            if (target.has(name)) {
                List<Object> currPath = getPath(path, name);
                computeUnchangedValues(unchangedValues, currPath, source.get(name), target.get(name));
            }
        }
    }
}
