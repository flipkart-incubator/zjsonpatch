package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class InternalUtils {

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    static List<JsonNode> toList(ArrayNode input) {
        int size = input.size();
        List<JsonNode> toReturn = new ArrayList<JsonNode>(size);
        for (int i = 0; i < size; i++) {
            toReturn.add(input.get(i));
        }
        return toReturn;
    }

    static List<JsonNode> longestCommonSubsequence(final List<JsonNode> a, final List<JsonNode> b) {
        if (a == null || b == null) {
            throw new NullPointerException("List must not be null for longestCommonSubsequence");
        }

        List<JsonNode> toReturn = new LinkedList<JsonNode>();

        int aSize = a.size();
        int bSize = b.size();
        int temp[][] = new int[aSize + 1][bSize + 1];

        for (int i = 1; i <= aSize; i++) {
            for (int j = 1; j <= bSize; j++) {
                if (i == 0 || j == 0) {
                    temp[i][j] = 0;
                } else if (a.get(i - 1).equals(b.get(j - 1))) {
                    temp[i][j] = temp[i - 1][j - 1] + 1;
                } else {
                    temp[i][j] = Math.max(temp[i][j - 1], temp[i - 1][j]);
                }
            }
        }
        int i = aSize, j = bSize;
        while (i > 0 && j > 0) {
            if (a.get(i - 1).equals(b.get(j - 1))) {
                toReturn.add(a.get(i - 1));
                i--;
                j--;
            } else if (temp[i - 1][j] > temp[i][j - 1])
                i--;
            else
                j--;
        }
        Collections.reverse(toReturn);
        return toReturn;
    }

    /**
     * Preprocessing step to produce clearer and more useful diffs of array elements.
     * Converts array fields with names present in the provided arrayKeyMap into objects
     * where the property names are generated from those specified in the arrayKeyMap.
     * This allows comparison of array items based on a certain key or keys rather than
     * just using the index in the array. This also aids in producing smaller patches.
     * Example:
     * arrayKeyMap: items -> name,val
     * Input: {
     *   "items": [
     *     {
     *       "name": "foo",
     *       "val": 1,
     *       "description": "Item 1"
     *     },
     *     {
     *       "name": "bar",
     *       "val": 2,
     *       "description": "Item 2"
     *     }
     *   ]
     * }
     * Output: {
     *   "items": {
     *     "[foo-1]": {
     *       "name": "foo",
     *       "val": 1,
     *       "description": "Item 1"
     *     },
     *     "[bar-2]": {
     *       "name": "bar",
     *       "val": 2,
     *       "description": "Item 2"
     *     }
     *   }
     * }
     * See {@link InternalUtils::arrayifyObjects} for inverse operation
     *
     * @param node
     * @param arrayKeyMap
     * @return output node
     */
    static JsonNode objectifyArrays(JsonNode node, java.util.Map<String, List<String>> arrayKeyMap) {
        for (Iterator i = node.fields(); i.hasNext(); ) {
            java.util.Map.Entry<String, JsonNode> field = (java.util.Map.Entry<String, JsonNode>)i.next();
            // Check if the node is an array
            if (field.getValue().getNodeType().equals(JsonNodeType.ARRAY)) {
                // Find the appropriate key to flatten by
                List<String> key = arrayKeyMap.getOrDefault(field.getKey(), null);
                if (null != key && !key.isEmpty()) {
                    ObjectNode newNode = new ObjectNode(factory);
                    for (int x = 0; x < field.getValue().size(); x++) {
                        // Check that the field at 'x' is actually an object, not a primitive
                        if (field.getValue().get(x).getNodeType().equals(JsonNodeType.OBJECT)) {
                            // Process array elements
                            JsonNode obj = field.getValue().get(x);

                            // Generate key value from the appropriate properties
                            String keyVal = key.stream()
                                    .map(k -> obj.get(k)!=null?obj.get(k).asText():null)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.joining("-"));
                            keyVal = '[' + (!keyVal.isEmpty()?keyVal:Integer.toString(x)) + ']';

                            // If the new node already has a value for this key it means the specified arrayKeyMap
                            // does not uniquely identify the items in the array. We should not continue in this case
                            // since information would be lost in the patch generation process.
                            if (newNode.get(keyVal) != null) {
                                throw new InvalidJsonPatchException("Key \"" + keyVal + "\" not unique when generated from \"" + key + "\" for property \"" + field.getKey() + "\". Could not generate Patch.");
                            }

                            // Set array element as value in new object node with the appropriate key
                            newNode.set(keyVal, objectifyArrays(field.getValue().get(x), arrayKeyMap));
                        }
                        else {
                            // Set array element as value in new object with index as key
                            newNode.set('[' + Integer.toString(x) + ']', field.getValue().get(x));
                        }
                    }
                    field.setValue(newNode);
                } else {
                    for (int x = 0; x < field.getValue().size(); x++) {
                        // Check that the field at 'x' is actually an object, not a primitive
                        if (field.getValue().get(x).getNodeType().equals(JsonNodeType.OBJECT)) {
                            objectifyArrays(field.getValue().get(x), arrayKeyMap);
                        }
                    }
                }
            }
            // If the field is an object process it recursively
            else if (field.getValue().getNodeType().equals(JsonNodeType.OBJECT)) {
                field.setValue(objectifyArrays(field.getValue(), arrayKeyMap));
            }
        }
        return node;
    }

    /**
     * Postprocessing step to convert arrays processed by {@link InternalUtils::objectifyArrays}
     * back into their original array format.
     * Converts object fields with names present in the provided arrayKeyMap into arrays.
     * See {@link InternalUtils::objectifyArrays} for inverse operation
     *
     * @param node
     * @param arrayKeyMap
     * @return output node
     */
    static JsonNode arrayifyObjects(JsonNode node, java.util.Map<String, List<String>> arrayKeyMap) {
        for (Iterator i = node.fields(); i.hasNext(); ) {
            java.util.Map.Entry<String, JsonNode> field = (java.util.Map.Entry<String, JsonNode>) i.next();
            // Check if the node is an object
            if (field.getValue().getNodeType().equals(JsonNodeType.OBJECT)) {
                // Find the appropriate key to unflatten by
                List<String> key = arrayKeyMap.getOrDefault(field.getKey(), null);
                if (null != key && !key.isEmpty()) {
                    ArrayNode newNode = new ArrayNode(factory);
                    // Add fields into list
                    Iterator<java.util.Map.Entry<String, JsonNode>> elements = field.getValue().fields();
                    boolean abort = false;
                    while (elements.hasNext()) {
                        java.util.Map.Entry<String, JsonNode> next = elements.next();
                        // Check that all the values match the pattern in order to be array-ified
                        if (!next.getKey().matches("\\[.*]")) {
                            abort = true;
                            break;
                        }
                        newNode.add(next.getValue());
                    }
                    if (!abort) {
                        field.setValue(newNode);
                    }
                } else {
                    Iterator<java.util.Map.Entry<String, JsonNode>> elements = field.getValue().fields();
                    while (elements.hasNext()) {
                        java.util.Map.Entry<String, JsonNode> next = elements.next();
                        if (next.getValue().getNodeType().equals(JsonNodeType.OBJECT) || next.getValue().getNodeType().equals(JsonNodeType.ARRAY)) {
                            arrayifyObjects(field.getValue(), arrayKeyMap);
                        }
                    }
                }
            }
            // If the field is an array process it recursively
            else if (field.getValue().getNodeType().equals(JsonNodeType.ARRAY)) {
                for (int x = 0; x < field.getValue().size(); x++) {
                    // Check that the field at 'x' is actually an object, not a primitive
                    if (field.getValue().get(x).getNodeType().equals(JsonNodeType.OBJECT)) {
                        arrayifyObjects(field.getValue().get(x), arrayKeyMap);
                    }
                }
            }
        }
        return node;
    }

    /**
     * Remove $id fields from the input node.
     *
     * @param node
     * @return output node
     */
    static JsonNode stripIds(JsonNode node) {
        ((ObjectNode) node).remove("$id");
        for (Iterator i = node.fields(); i.hasNext(); ) {
            java.util.Map.Entry<String, JsonNode> field = (java.util.Map.Entry<String, JsonNode>)i.next();
            // Check if the node is an array
            if (field.getValue().getNodeType().equals(JsonNodeType.ARRAY)) {
                for (int x = 0; x < field.getValue().size(); x++) {
                    // Check that the field at 'x' is actually an object, not a primitive
                    if (field.getValue().get(x).getNodeType().equals(JsonNodeType.OBJECT)) {
                        stripIds(field.getValue().get(x));
                    }
                }
            }
            // If the field is an object process it recursively
            if (field.getValue().getNodeType().equals(JsonNodeType.OBJECT)) {
                field.setValue(stripIds(field.getValue()));
            }
        }
        return node;
    }
}
