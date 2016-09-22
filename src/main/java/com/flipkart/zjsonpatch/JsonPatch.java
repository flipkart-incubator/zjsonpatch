package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * User: gopi.vishwakarma Date: 31/07/14
 */
public final class JsonPatch {

    private JsonPatch() {
    }

    private static JsonNode getPatchAttr(JsonNode jsonNode, String attr) {
        JsonNode child = jsonNode.get(attr);
        if (child == null)
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
        return child;
    }

    private static JsonNode getPatchAttrWithDefault(JsonNode jsonNode, String attr, JsonNode defaultValue) {
        JsonNode child = jsonNode.get(attr);
        if (child == null)
            return defaultValue;
        else
            return child;
    }

    private static void process(JsonNode patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
            throws InvalidJsonPatchException {

        if (!patch.isArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
        Iterator<JsonNode> operations = patch.iterator();
        while (operations.hasNext()) {
            JsonNode jsonNode = operations.next();
            if (!jsonNode.isObject())
                throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchAttr(jsonNode, Constants.OP).asText());
            List<String> path = getPath(getPatchAttr(jsonNode, Constants.PATH).asText());

            switch (operation) {
            case REMOVE: {
                processor.remove(path);
                break;
            }

            case ADD: {
                JsonNode value;
                if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                    value = getPatchAttr(jsonNode, Constants.VALUE);
                else
                    value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                processor.add(path, value);
                break;
            }

            case REPLACE: {
                JsonNode value;
                if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                    value = getPatchAttr(jsonNode, Constants.VALUE);
                else
                    value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                processor.replace(path, value);
                break;
            }

            case MOVE: {
                List<String> fromPath = getPath(getPatchAttr(jsonNode, Constants.FROM).asText());
                processor.move(fromPath, path);
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
        ApplyProcessor processor =
                new ApplyProcessor(flags.contains(CompatibilityFlags.PATCH_IN_PLACE) ? source : source.deepCopy());
        process(patch, processor, flags);
        return processor.result();
    }

    public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }

    private static List<String> getPath(String path) {
        List<String> paths = new ArrayList<String>();
        int index = 0, last = 0, len = path.length();
        while (index < len) {
            if (path.charAt(index) == '/') {
                paths.add(decodeSubPath(path.substring(last, index)));
                last = ++index;
            } else {
                index++;
            }
        }
        paths.add(decodeSubPath(path.substring(last, index)));
        return paths;
    }

    private static String decodeSubPath(String path) {
        // see http://tools.ietf.org/html/rfc6901#section-4
        return path.replaceAll("~1", "/").replaceAll("~0", "~");
    }
}
