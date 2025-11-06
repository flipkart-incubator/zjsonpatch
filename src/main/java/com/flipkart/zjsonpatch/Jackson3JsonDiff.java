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
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;
import com.flipkart.zjsonpatch.mapping.jackson3.Jackson3NodeFactory;
import tools.jackson.databind.JsonNode;

import java.util.EnumSet;
import java.util.List;

/**
 * Jackson 3.x compatible JSON Diff implementation.
 * This class provides JSON difference computation functionality using Jackson 3.x APIs.
 * 
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public final class Jackson3JsonDiff extends AbstractJsonDiff {

    private static final JsonNodeFactoryWrapper FACTORY = new Jackson3NodeFactory();

    private Jackson3JsonDiff(EnumSet<DiffFlags> flags) {
        super(flags);
    }

    @Override
    protected Jackson3JsonPointer getJsonPointerRoot() {
        return Jackson3JsonPointer.ROOT;
    }

    @Override
    protected Jackson3JsonPointer createJsonPointerInstance(List<RefToken> tokens) {
        return new Jackson3JsonPointer(tokens);
    }

    public static JsonNode asJson(final JsonNode source, final JsonNode target) {
        return asJson(source, target, DiffFlags.defaults());
    }

    public static JsonNode asJson(final JsonNode source, final JsonNode target, EnumSet<DiffFlags> flags) {
        JsonNodeWrapper sourceWrapper = JacksonVersionBridge.wrap(source);
        JsonNodeWrapper targetWrapper = JacksonVersionBridge.wrap(target);
        return JacksonVersionBridge.unwrap(getJsonNode(sourceWrapper, targetWrapper, new Jackson3JsonDiff(flags), FACTORY));
    }
}
