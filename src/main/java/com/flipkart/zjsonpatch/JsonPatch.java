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

package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

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
            }
        }
    }

    public static void validate(JsonNode patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
        process(patch, NoopProcessor.INSTANCE, flags);
    }

    public static void validate(JsonNode patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    /**
     * Applies a JSON Patch to a given source document using default 
     * {@link CompatibilityFlags} and an
     * <a href="https://tools.ietf.org/html/rfc6902#section-5">RFC6902-compliant 
     * error handling</a> strategy.
     * @param patch JSON Patch to apply
     * @param source JSON document to patch.
     * @return Patched clone of {@code source} document.
     * @throws JsonPatchApplicationException if an error occurs while patching.
     */
    public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }
    
    /**
     * Applies a JSON Patch to a given source document using a specified set of
     * {@link CompatibilityFlags} and an
     * <a href="https://tools.ietf.org/html/rfc6902#section-5">RFC6902-compliant 
     * error handling</a> strategy.
     * @param patch JSON Patch to apply
     * @param source JSON document to patch.
     * @param flags flags modifying patch behavior.
     * @return Patched clone of {@code source} document.
     * @throws JsonPatchApplicationException if an error occurs while patching.
     */
    public static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) throws JsonPatchApplicationException {
        return apply(patch, source, flags, ExceptionErrorHandlingStrategy.INSTANCE);
    }
    
    /**
     * Applies a JSON Patch to a given source document using a specified set of
     * {@link CompatibilityFlags} and a user-defined error handling strategy.
     * @param patch JSON Patch to apply
     * @param source JSON document to patch.
     * @param flags flags modifying patch behavior.
     * @param errorStrategy User-define error handling strategy.
     * @return Patched clone of {@code source} document.
     * @throws JsonPatchApplicationException if an error occurs while patching.
     */
    public static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags, JsonPatchErrorHandlingStrategy errorStrategy) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source.deepCopy(), flags, errorStrategy);
        process(patch, processor, flags);
        return processor.result();
    }
       
    /**
     * Applies an in-place JSON Patch to a given source document using default 
     * {@link CompatibilityFlags} and an
     * <a href="https://tools.ietf.org/html/rfc6902#section-5">RFC6902-compliant 
     * error handling</a> strategy.
     * @param patch JSON Patch to apply
     * @param source JSON document to patch.
     * @throws JsonPatchApplicationException if an error occurs while patching.
     */
    public static void applyInPlace(JsonNode patch, JsonNode source) {
        applyInPlace(patch, source, CompatibilityFlags.defaults());
    }
    
    /**
     * Applies an in-place JSON Patch to a given source document using a specified set of
     * {@link CompatibilityFlags} and an
     * <a href="https://tools.ietf.org/html/rfc6902#section-5">RFC6902-compliant 
     * error handling</a> strategy.
     * @param patch JSON Patch to apply
     * @param source JSON document to patch.
     * @param flags flags modifying patch behavior.
     * @throws JsonPatchApplicationException if an error occurs while patching.
     */
    public static void applyInPlace(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) {
        applyInPlace(patch, source, flags, ExceptionErrorHandlingStrategy.INSTANCE);
    }   
    
    /**
     * Applies an in-place JSON Patch to a given source document using a specified set of
     * {@link CompatibilityFlags} and a user-defined error handling strategy.
     * This method will mutate the original {@code source} document.
     * @param patch JSON Patch to apply
     * @param source JSON document to patch.
     * @param flags flags modifying patch behavior.
     * @param errorStrategy User-define error handling strategy.
     * @throws JsonPatchApplicationException if an error occurs while patching.
     */
    public static void applyInPlace(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags, JsonPatchErrorHandlingStrategy errorStrategy) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source, flags, errorStrategy);
        process(patch, processor, flags);
    }
}
