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

package com.flipkart.zjsonpatch.mapping.jackson3;

import com.flipkart.zjsonpatch.mapping.ArrayNodeWrapper;
import com.flipkart.zjsonpatch.mapping.JsonNodeFactoryWrapper;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;
import com.flipkart.zjsonpatch.mapping.ObjectNodeWrapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NullNode;

/**
 * Jackson 3.x implementation of {@link JsonNodeFactoryWrapper}.
 *
 * <p>This class is stateless and thread-safe. It can be safely used as a singleton
 * because it only delegates to Jackson's thread-safe {@link JsonNodeFactory#instance}
 * and creates new wrapper instances for each method call.</p>
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class Jackson3NodeFactory implements JsonNodeFactoryWrapper {
    
    private final JsonNodeFactory factory = JsonNodeFactory.instance;
    
    @Override
    public ArrayNodeWrapper arrayNode() {
        return new Jackson3ArrayNodeWrapper(factory.arrayNode());
    }
    
    @Override
    public ObjectNodeWrapper objectNode() {
        return new Jackson3ObjectNodeWrapper(factory.objectNode());
    }
    
    @Override
    public JsonNodeWrapper nullNode() {
        return new Jackson3NodeWrapper(NullNode.getInstance());
    }
    
    @Override
    public JsonNodeWrapper textNode(String text) {
        return new Jackson3NodeWrapper(factory.stringNode(text));
    }
}
