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

import com.flipkart.zjsonpatch.mapping.ArrayNodeWrapper;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;
import com.flipkart.zjsonpatch.mapping.ObjectNodeWrapper;

import java.util.EnumSet;

class InPlaceApplyProcessor implements JsonPatchProcessor {

    private JsonNodeWrapper target;
    private EnumSet<CompatibilityFlags> flags;

    // Internal wrapper-based constructors 
    InPlaceApplyProcessor(JsonNodeWrapper target) {
        this(target, CompatibilityFlags.defaults());
    }

    InPlaceApplyProcessor(JsonNodeWrapper target, EnumSet<CompatibilityFlags> flags) {
        this.target = target;
        this.flags = flags;
    }

    public JsonNodeWrapper result() {
        return target;
    }

    protected boolean allowRootReplacement() {
        return false;
    }

    @Override
    public void move(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
        JsonNodeWrapper valueNode = fromPath.evaluate(target);
        remove(fromPath);
        set(toPath, valueNode, Operation.MOVE);
    }

    @Override
    public void copy(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
        JsonNodeWrapper valueNode = fromPath.evaluate(target);
        JsonNodeWrapper valueToCopy = valueNode != null ? valueNode.deepCopy() : null;
        set(toPath, valueToCopy, Operation.COPY);
    }

    private static String show(JsonNodeWrapper value) {
        if (value == null || value.isNull())
            return "null";
        else if (value.isArray())
            return "array";
        else if (value.isObject())
            return "object";
        else
            return "value " + value.toString();     // Caveat: numeric may differ from source (e.g. trailing zeros)
    }

    @Override
    public void test(JsonPointer path, JsonNodeWrapper value) throws JsonPointerEvaluationException {
        JsonNodeWrapper valueNode = path.evaluate(target);
        if (!valueNode.equals(value))
            throw new JsonPatchApplicationException(
                    "Expected " + show(value) + " but found " + show(valueNode), Operation.TEST, path);
    }

    @Override
    public void add(JsonPointer path, JsonNodeWrapper value) throws JsonPointerEvaluationException {
        set(path, value, Operation.ADD);
    }

    @Override
    public void replace(JsonPointer path, JsonNodeWrapper value) throws JsonPointerEvaluationException {
        if (path.isRoot()) {
            if (!allowRootReplacement())
                throw new JsonPatchApplicationException("Cannot replace root document", Operation.REPLACE, path);
            target = value;
            return;
        }

        JsonNodeWrapper parentNode = path.getParent().evaluate(target);
        RefToken token = path.last();
        if (parentNode.isObject()) {
            if (!flags.contains(CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE) &&
                    !parentNode.has(token.getField()))
                throw new JsonPatchApplicationException(
                        "Missing field \"" + token.getField() + "\"", Operation.REPLACE, path.getParent());
            parentNode.objectValue().replace(token.getField(), value);
        } else if (parentNode.isArray()) {
            if (token.getIndex() >= parentNode.size())
                throw new JsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REPLACE, path.getParent());
            parentNode.arrayValue().set(token.getIndex(), value);
        } else {
            throw new JsonPatchApplicationException(
                    "Can't reference past scalar value", Operation.REPLACE, path.getParent());
        }
    }

    @Override
    public void remove(JsonPointer path) throws JsonPointerEvaluationException {
        if (path.isRoot())
            throw new JsonPatchApplicationException("Cannot remove document root", Operation.REMOVE, path);

        JsonNodeWrapper parentNode = path.getParent().evaluate(target);
        RefToken token = path.last();
        if (parentNode.isObject()) {
            if (flags.contains(CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT) && !parentNode.has(token.getField()))
                throw new JsonPatchApplicationException(
                        "Missing field " + token.getField(), Operation.REMOVE, path.getParent());
            parentNode.objectValue().remove(token.getField());
        }
        else if (parentNode.isArray()) {
            if (!flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT) &&
                    token.getIndex() >= parentNode.size())
                throw new JsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REMOVE, path.getParent());
            parentNode.arrayValue().remove(token.getIndex());
        } else {
            throw new JsonPatchApplicationException(
                    "Cannot reference past scalar value", Operation.REMOVE, path.getParent());
        }
    }



    private void set(JsonPointer path, JsonNodeWrapper value, Operation forOp) throws JsonPointerEvaluationException {
        if (path.isRoot()) {
            if (!allowRootReplacement())
                throw new JsonPatchApplicationException("Cannot replace root document", forOp, path);
            target = value;
            return;
        }

        JsonNodeWrapper parentNode = path.getParent().evaluate(target);
        if (!parentNode.isContainerNode())
            throw new JsonPatchApplicationException("Cannot reference past scalar value", forOp, path.getParent());
        else if (parentNode.isArray())
            addToArray(path, value, parentNode);
        else
            addToObject(path, parentNode, value);
    }

    private void addToObject(JsonPointer path, JsonNodeWrapper node, JsonNodeWrapper value) {
        final ObjectNodeWrapper target = node.objectValue();
        String key = path.last().getField();
        target.set(key, value);
    }

    private void addToArray(JsonPointer path, JsonNodeWrapper value, JsonNodeWrapper parentNode) {
        final ArrayNodeWrapper target = parentNode.arrayValue();
        int idx = path.last().getIndex();

        if (idx == JsonPointer.LAST_INDEX) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            if (idx > target.size())
                throw new JsonPatchApplicationException(
                        "Array index " + idx + " out of bounds", Operation.ADD, path.getParent());
            target.insert(idx, value);
        }
    }
}
