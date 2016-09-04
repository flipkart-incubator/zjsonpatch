package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public final class JsonPatch {

    private static final DecodePathFunction DECODE_PATH_FUNCTION = new DecodePathFunction();

    private JsonPatch() {}

    private final static class DecodePathFunction implements Function<String, String> {
        @Override
        public String apply(String path) {
            return path.replaceAll("~1", "/").replaceAll("~0", "~"); // see http://tools.ietf.org/html/rfc6901#section-4
        }
    }

    private static JsonNode getPatchAttr(JsonNode jsonNode, String attr) {
        JsonNode child = jsonNode.get(attr);
        if (child == null)
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
        return child;
    }

    private static void process(JsonNode patch, JsonPatchProcessor processor, int compatibilityFlags)
            throws InvalidJsonPatchException {

        if (!patch.isArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
        Iterator<JsonNode> operations = patch.iterator();
        while (operations.hasNext()) {
            JsonNode jsonNode = operations.next();
            if (!jsonNode.isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchAttr(jsonNode, Constants.OP).toString().replaceAll("\"", ""));
            List<String> path = getPath(getPatchAttr(jsonNode, Constants.PATH));
            List<String> fromPath = null;
            if (Operation.MOVE.equals(operation)) {
                fromPath = getPath(getPatchAttr(jsonNode, Constants.FROM));
            }
            JsonNode value = null;
            if (!Operation.REMOVE.equals(operation) && !Operation.MOVE.equals(operation)) {
                value = getPatchAttr(jsonNode, Constants.VALUE);
            }

            switch (operation) {
                case REMOVE:
                    processor.remove(path);
                    break;
                case REPLACE:
                    processor.replace(path, value);
                    break;
                case ADD:
                    processor.add(path, value);
                    break;
                case MOVE:
                    processor.move(fromPath, path);
                    break;
            }
        }
    }

    public static void validate(JsonNode patch, int compatibilityFlags) throws InvalidJsonPatchException {
        process(patch, NoopProcessor.INSTANCE, compatibilityFlags);
    }

    public static void validate(JsonNode patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.DEFAULTS);
    }

    public static JsonNode apply(JsonNode patch, JsonNode source, int compatibilityFlags) throws JsonPatchApplicationException {
        ApplyProcessor processor = new ApplyProcessor(source);
        process(patch, processor, compatibilityFlags);
        return processor.result();
    }

    public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.DEFAULTS);
    }

    private static List<String> getPath(JsonNode path) {
        List<String> paths = Splitter.on('/').splitToList(path.toString().replaceAll("\"", ""));
        return Lists.newArrayList(Iterables.transform(paths, DECODE_PATH_FUNCTION));
    }
}
