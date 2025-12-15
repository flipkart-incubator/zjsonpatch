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

import com.flipkart.zjsonpatch.mapping.*;
import tools.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class Jackson3ObjectNodeWrapper extends Jackson3NodeWrapper implements ObjectNodeWrapper {
    
    private final ObjectNode objectNode;
    
    public Jackson3ObjectNodeWrapper(ObjectNode objectNode) {
        super(objectNode);
        this.objectNode = objectNode;
    }
    
    @Override
    public JsonNodeWrapper put(String fieldName, JsonNodeWrapper value) {
        if (value == null) {
            objectNode.putNull(fieldName);
            return this;
        }
        Jackson3NodeWrapper jackson3Value = (Jackson3NodeWrapper) value;
        objectNode.set(fieldName, jackson3Value.getJackson3Node());
        return this;
    }
    
    @Override
    public JsonNodeWrapper set(String fieldName, JsonNodeWrapper value) {
        if (value == null) {
            objectNode.set(fieldName, null);
            return this;
        }
        Jackson3NodeWrapper jackson3Value = (Jackson3NodeWrapper) value;
        objectNode.set(fieldName, jackson3Value.getJackson3Node());
        return this;
    }
    
    @Override
    public JsonNodeWrapper replace(String fieldName, JsonNodeWrapper value) {
        if (value == null) {
            tools.jackson.databind.JsonNode result = objectNode.replace(fieldName, null);
            return result != null ? new Jackson3NodeWrapper(result) : null;
        }
        tools.jackson.databind.JsonNode result = objectNode.replace(fieldName, ((Jackson3NodeWrapper) value).getJackson3Node());
        return result != null ? new Jackson3NodeWrapper(result) : null;
    }
    
    @Override
    public JsonNodeWrapper remove(String fieldName) {
        tools.jackson.databind.JsonNode result = objectNode.remove(fieldName);
        return result != null ? new Jackson3NodeWrapper(result) : null;
    }
    

    @Override
    public Iterator<JsonNodeWrapper> iterator() {
        return new IteratorDecorator<>(objectNode.iterator());
    }
}
