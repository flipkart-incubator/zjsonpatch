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

package com.flipkart.zjsonpatch.mapping.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.mapping.*;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Iterator;
import java.util.ListIterator;

/**
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class Jackson2ArrayNodeWrapper extends Jackson2NodeWrapper implements ArrayNodeWrapper {
    
    private final ArrayNode arrayNode;
    
    public Jackson2ArrayNodeWrapper(ArrayNode arrayNode) {
        super(arrayNode);
        this.arrayNode = arrayNode;
    }
    
    @Override
    public JsonNodeWrapper set(int index, JsonNodeWrapper value) {
        Jackson2NodeWrapper jackson2Value = (Jackson2NodeWrapper) value;
        arrayNode.set(index, jackson2Value.getJackson2Node());
        return this;
    }
    
    @Override
    public ArrayNodeWrapper add(JsonNodeWrapper value) {
        Jackson2NodeWrapper jackson2Value = (Jackson2NodeWrapper) value;
        arrayNode.add(jackson2Value.getJackson2Node());
        return this;
    }
    
    @Override
    public ArrayNodeWrapper insert(int index, JsonNodeWrapper value) {
        Jackson2NodeWrapper jackson2Value = (Jackson2NodeWrapper) value;
        arrayNode.insert(index, jackson2Value.getJackson2Node());
        return this;
    }
    
    @Override
    public JsonNodeWrapper remove(int index) {
        return new Jackson2NodeWrapper(arrayNode.remove(index));
    }
    

    @Override
    public Iterator<JsonNodeWrapper> iterator() {
        return new ListIteratorDecorator<>((ListIterator<JsonNode>) arrayNode.iterator());
    }
}
