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
import com.flipkart.zjsonpatch.mapping.IteratorDecorator;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;
import tools.jackson.databind.node.ArrayNode;

import java.util.Iterator;

/**
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class Jackson3ArrayNodeWrapper extends Jackson3NodeWrapper implements ArrayNodeWrapper {
    
    private final ArrayNode arrayNode;
    
    public Jackson3ArrayNodeWrapper(ArrayNode arrayNode) {
        super(arrayNode);
        this.arrayNode = arrayNode;
    }
    
    @Override
    public JsonNodeWrapper set(int index, JsonNodeWrapper value) {
        Jackson3NodeWrapper jackson3Value = (Jackson3NodeWrapper) value;
        arrayNode.set(index, jackson3Value.getJackson3Node());
        return this;
    }
    
    @Override
    public ArrayNodeWrapper add(JsonNodeWrapper value) {
        Jackson3NodeWrapper jackson3Value = (Jackson3NodeWrapper) value;
        arrayNode.add(jackson3Value.getJackson3Node());
        return this;
    }
    
    @Override
    public ArrayNodeWrapper insert(int index, JsonNodeWrapper value) {
        Jackson3NodeWrapper jackson3Value = (Jackson3NodeWrapper) value;
        arrayNode.insert(index, jackson3Value.getJackson3Node());
        return this;
    }
    
    @Override
    public JsonNodeWrapper remove(int index) {
        return new Jackson3NodeWrapper(arrayNode.remove(index));
    }
    

    @Override
    public Iterator<JsonNodeWrapper> iterator() {
        return new IteratorDecorator<>(arrayNode.iterator());
    }
}
