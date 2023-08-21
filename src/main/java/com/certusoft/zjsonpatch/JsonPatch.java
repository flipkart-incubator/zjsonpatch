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

package com.certusoft.zjsonpatch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
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
            if (!jsonNode.isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchAttr(jsonNode, Constants.OP).toString().replaceAll("\"", ""));
            List<String> path = PathUtils.getPath(getPatchAttr(jsonNode, Constants.PATH));

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
                    processor.add(path, value.deepCopy());
                    break;
                }

                case REPLACE: {
                    JsonNode value;
                    if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                        value = getPatchAttr(jsonNode, Constants.VALUE);
                    else
                        value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                    processor.replace(path, value.deepCopy());
                    break;
                }

                case MOVE: {
                    List<String> fromPath = PathUtils.getPath(getPatchAttr(jsonNode, Constants.FROM));
                    processor.move(fromPath, path);
                    break;
                }

                case COPY: {
                    List<String> fromPath = PathUtils.getPath(getPatchAttr(jsonNode, Constants.FROM));
                    processor.copy(fromPath, path);
                    break;
                }

                case TEST: {
                    JsonNode value;
                    if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                        value = getPatchAttr(jsonNode, Constants.VALUE);
                    else
                        value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
                    processor.test(path, value.deepCopy());
                    break;
                }

                case METADATA: {
                    // Do nothing
                    break;
                }
            }
        }
    }

    private static void preProcess(JsonNode patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags, ObjectMapper om)
            throws InvalidJsonPatchException {
        if (!patch.isArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");

        List<JsonNode> metadata = StreamSupport.stream(patch.spliterator(), false)
                .filter(x -> x.get(Constants.OP).asText().equals(Operation.METADATA.rfcName()))
                .collect(Collectors.toList());

        if (flags.contains(CompatibilityFlags.OBJECTIFY_ARRAYS) && !metadata.isEmpty()) {

            // Turn arrays into objects based on provided keyMap
            JsonNode keyMapMetadata = metadata.stream()
                    .filter(x -> x.get(Constants.PATH).asText().equals(DiffFlags.OBJECTIFY_ARRAYS.toString()))
                    .findFirst().orElse(null);

            if (null != keyMapMetadata) {
                try {
                    Map<String, List<String>> arrayKeyMap = om.readValue(keyMapMetadata.get(Constants.VALUE).textValue(), new TypeReference<HashMap>() {});
                    processor.objectifyArrays(arrayKeyMap);
                } catch (Exception e) {
                    throw new InvalidJsonPatchException("Failed to parse metadata: ", e);
                }
            }
        }
    }

    private static void postProcess(JsonNode patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags, ObjectMapper om)
            throws InvalidJsonPatchException {
        if (!patch.isArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");

        List<JsonNode> metadata = StreamSupport.stream(patch.spliterator(), false)
                .filter(x -> x.get(Constants.OP).asText().equals(Operation.METADATA.rfcName()))
                .collect(Collectors.toList());

        if (flags.contains(CompatibilityFlags.IGNORE_ID)) {

            // Strip $id values from json structure

            processor.stripIds();
        }

        if (flags.contains(CompatibilityFlags.OBJECTIFY_ARRAYS) && !metadata.isEmpty()) {

            // Turn arrays into objects based on provided keyMap
            JsonNode keyMapMetadata = metadata.stream()
                    .filter(x -> x.get(Constants.PATH).asText().equals(DiffFlags.OBJECTIFY_ARRAYS.toString()))
                    .findFirst().orElse(null);

            if (null != keyMapMetadata) {
                try {
                    Map<String, List<String>> arrayKeyMap = om.readValue(keyMapMetadata.get(Constants.VALUE).textValue(), new TypeReference<HashMap>() {});
                    processor.arrayifyObjects(arrayKeyMap);
                } catch (Exception e) {
                    throw new InvalidJsonPatchException("Failed to parse metadata: ", e);
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

    public static JsonNode apply(JsonPatchParams params) throws JsonPatchApplicationException {
        return apply(params.patch, params.source, params.flags, params.om);
    }

    private static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags, ObjectMapper om) throws JsonPatchApplicationException {
        CopyingApplyProcessor processor = new CopyingApplyProcessor(source, flags);
        preProcess(patch, processor, flags, om);
        process(patch, processor, flags);
        postProcess(patch, processor, flags, om);
        return processor.result();
    }

    public static void applyInPlace(JsonPatchParams params) {
        applyInPlace(params.patch, params.source, params.flags, params.om);
    }

    private static void applyInPlace(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags, ObjectMapper om) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source, flags);
        preProcess(patch, processor, flags, om);
        process(patch, processor, flags);
        postProcess(patch, processor, flags, om);
    }

    public static class JsonPatchParams {
        final JsonNode patch;
        final JsonNode source;
        EnumSet<CompatibilityFlags> flags;
        ObjectMapper om;

        public JsonPatchParams(JsonNode patch, JsonNode source) {
            this.patch = patch;
            this.source = source;
            this.flags = CompatibilityFlags.defaults();
            this.om = new ObjectMapper();
        }
        public JsonPatchParams flags(EnumSet<CompatibilityFlags> flags) {
            this.flags = flags;
            return this;
        }
        public JsonPatchParams om(ObjectMapper om) {
            this.om = om;
            return this;
        }
    }
}
