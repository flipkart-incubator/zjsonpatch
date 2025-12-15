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

import com.flipkart.zjsonpatch.mapping.JsonNodeFactoryWrapper;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;

import java.util.EnumSet;
import java.util.Iterator;

/**
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public abstract sealed class AbstractJsonPatch permits JsonPatch, Jackson3JsonPatch {

    protected AbstractJsonPatch() {
    }

    private static JsonNodeWrapper getPatchStringAttr(JsonNodeWrapper jsonNode, String attr) {
        JsonNodeWrapper child = getPatchAttr(jsonNode, attr);

        if (!child.isTextual())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (non-text '" + attr + "' field)");

        return child;
    }

    private static JsonNodeWrapper getPatchAttr(JsonNodeWrapper jsonNode, String attr) {
        JsonNodeWrapper child = jsonNode.get(attr);
        if (child == null)
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");

        return child;
    }

    private static JsonNodeWrapper getPatchAttrWithDefault(JsonNodeWrapper jsonNode, String attr, JsonNodeWrapper defaultValue) {
        JsonNodeWrapper child = jsonNode.get(attr);
        if (child == null)
            return defaultValue;
        else
            return child;
    }

    protected static void process(JsonNodeWrapper patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags, JsonNodeFactoryWrapper factory)
            throws InvalidJsonPatchException {

        if (!patch.isArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
        Iterator<JsonNodeWrapper> operations = patch.iterator();
        while (operations.hasNext()) {
            JsonNodeWrapper jsonNode = operations.next();
            if (!jsonNode.isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchStringAttr(jsonNode, Constants.OP).textValue());
            JsonPointer path = JsonPointer.parse(getPatchStringAttr(jsonNode, Constants.PATH).textValue());

            try {
                switch (operation) {
                    case REMOVE: {
                        processor.remove(path);
                        break;
                    }

                    case ADD: {
                        JsonNodeWrapper value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, factory.nullNode());
                        processor.add(path, value.deepCopy());
                        break;
                    }

                    case REPLACE: {
                        JsonNodeWrapper value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, factory.nullNode());
                        processor.replace(path, value.deepCopy());
                        break;
                    }

                    case MOVE: {
                        JsonPointer fromPath = JsonPointer.parse(getPatchStringAttr(jsonNode, Constants.FROM).textValue());
                        processor.move(fromPath, path);
                        break;
                    }

                    case COPY: {
                        JsonPointer fromPath = JsonPointer.parse(getPatchStringAttr(jsonNode, Constants.FROM).textValue());
                        processor.copy(fromPath, path);
                        break;
                    }

                    case TEST: {
                        JsonNodeWrapper value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, factory.nullNode());
                        processor.test(path, value.deepCopy());
                        break;
                    }
                }
            }
            catch (JsonPointerEvaluationException e) {
                throw new JsonPatchApplicationException(e.getMessage(), operation, e.getPath());
            }
        }
    }
}
