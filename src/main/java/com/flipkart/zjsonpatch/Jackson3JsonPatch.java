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

import com.flipkart.zjsonpatch.mapping.JacksonVersionBridge;
import com.flipkart.zjsonpatch.mapping.JsonNodeFactoryWrapper;
import com.flipkart.zjsonpatch.mapping.jackson3.Jackson3NodeFactory;
import tools.jackson.databind.JsonNode;

import java.util.EnumSet;

/**
 * Jackson 3.x compatible JSON Patch implementation.
 * This class provides JSON patch application functionality using Jackson 3.x APIs.
 * 
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public final class Jackson3JsonPatch extends AbstractJsonPatch {

    private static final JsonNodeFactoryWrapper FACTORY = new Jackson3NodeFactory();

    private Jackson3JsonPatch() {
    }

    public static void validate(JsonNode patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
        process(JacksonVersionBridge.wrap(patch), NoopProcessor.INSTANCE, flags, FACTORY);
    }

    public static void validate(JsonNode patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    public static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) throws JsonPatchApplicationException {
        CopyingApplyProcessor processor = new CopyingApplyProcessor(JacksonVersionBridge.wrap(source), flags);
        process(JacksonVersionBridge.wrap(patch), processor, flags, FACTORY);
        return JacksonVersionBridge.unwrap(processor.result());
    }

    public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonNode patch, JsonNode source) {
        applyInPlace(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(JacksonVersionBridge.wrap(source), flags);
        process(JacksonVersionBridge.wrap(patch), processor, flags, FACTORY);
    }
}
