package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * User: gopi.vishwakarma Date: 31/07/14
 */
public final class JsonPatch {

    private JsonPatch() {
    }

    private static JsonNode getPatchAttr(JsonNode node, String attr) {
        JsonNode child = node.get(attr);
        if (child == null) {
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
        }
        return child;
    }

    private static JsonNode getPatchAttrWithDefault(JsonNode node, String attr, JsonNode defaultValue) {
        JsonNode child = node.get(attr);
        return (child == null) ? defaultValue : child;
    }

    private static JsonNode getPatchAttr(JsonNode node, EnumSet<CompatibilityFlags> flags) {
        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS)) {
            return getPatchAttr(node, Constants.VALUE);
        } else {
            return getPatchAttrWithDefault(node, Constants.VALUE, NullNode.getInstance());
        }
    }

    private static void process(JsonNode patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
            throws InvalidJsonPatchException {

        if (!patch.isArray()) {
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
        }
        Iterator<JsonNode> operations = patch.iterator();
        while (operations.hasNext()) {
            JsonNode node = operations.next();
            if (!node.isObject()) {
                throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            }
            Operation operation = Operation.fromRfcName(getPatchAttr(node, Constants.OP).asText());
            List<String> path = JsonPathHelper.getPath(getPatchAttr(node, Constants.PATH).asText());

            switch (operation) {
                case ADD: {
                    JsonNode value = getPatchAttr(node, flags);
                    processor.add(path, value);
                    break;
                }

                case TEST: {
                    JsonNode value = getPatchAttr(node, flags);
                    processor.test(path, value);
                    break;
                }

                case REPLACE: {
                    JsonNode value = getPatchAttr(node, flags);
                    processor.replace(path, value);
                    break;
                }

                case REMOVE: {
                    processor.remove(path);
                    break;
                }

                case MOVE: {
                    List<String> fromPath = JsonPathHelper.getPath(getPatchAttr(node, Constants.FROM).asText());
                    processor.move(fromPath, path);
                    break;
                }

                case COPY: {
                    List<String> fromPath = JsonPathHelper.getPath(getPatchAttr(node, Constants.FROM).asText());
                    processor.copy(fromPath, path);
                    break;
                }
            }
        }
    }

    public static void validate(JsonNode patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
        process(patch, NoopProcessor.INSTANCE, flags);
    }

    public static void validate(JsonNode patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    public static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags)
            throws JsonPatchApplicationException {
        ApplyProcessor processor = new ApplyProcessor(
                flags.contains(CompatibilityFlags.ENABLE_PATCH_IN_PLACE) ? source : source.deepCopy());
        process(patch, processor, flags);
        return processor.result();
    }

    public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }
}
