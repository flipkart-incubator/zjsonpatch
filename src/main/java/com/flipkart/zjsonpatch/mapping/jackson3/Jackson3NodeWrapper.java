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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class Jackson3NodeWrapper implements JsonNodeWrapper {
    
    private final JsonNode node;
    
    public Jackson3NodeWrapper(JsonNode node) {
        this.node = node;
    }
    
    @Override
    public boolean isArray() {
        return node.isArray();
    }
    
    @Override
    public boolean isObject() {
        return node.isObject();
    }
    
    @Override
    public boolean isTextual() {
        return node.isString();
    }
    
    @Override
    public boolean isNumber() {
        return node.isNumber();
    }
    
    @Override
    public boolean isBoolean() {
        return node.isBoolean();
    }
    
    @Override
    public boolean isNull() {
        return node.isNull();
    }
    
    @Override
    public boolean isContainerNode() {
        return node.isArray() || node.isObject();
    }
    
    @Override
    public JsonNodeWrapper get(String fieldName) {
        JsonNode result = node.get(fieldName);
        return result != null ? new Jackson3NodeWrapper(result) : null;
    }
    
    @Override
    public JsonNodeWrapper get(int index) {
        JsonNode result = node.get(index);
        return result != null ? new Jackson3NodeWrapper(result) : null;
    }
    
    @Override
    public boolean has(String fieldName) {
        return node.has(fieldName);
    }
    
    @Override
    public String textValue() {
        return node.stringValue();
    }
    
    
    @Override
    public int size() {
        return node.size();
    }
    
    @Override
    public Iterator<String> fieldNames() {
        if (node.isObject()) {
            return node.propertyNames().iterator();
        }
        return java.util.Collections.emptyIterator();
    }

    @Override
    public Iterator<JsonNodeWrapper> iterator() {
        return new IteratorDecorator<>(node.iterator());
    }
    
    @Override
    public ArrayNodeWrapper arrayValue() {
        if (node.isArray()) {
            return new Jackson3ArrayNodeWrapper((ArrayNode) node);
        }
        return null;
    }
    
    @Override
    public ObjectNodeWrapper objectValue() {
        if (node.isObject()) {
            return new Jackson3ObjectNodeWrapper((ObjectNode) node);
        }
        return null;
    }
    
    @Override
    public JsonNodeWrapper deepCopy() {
        return new Jackson3NodeWrapper(node.deepCopy());
    }
    
    @Override
    public JsonTokenWrapper asToken() {
        return mapToken(node.asToken());
    }
    
    private JsonTokenWrapper mapToken(tools.jackson.core.JsonToken token) {
        if (token == null) return null;
        
        if (token == tools.jackson.core.JsonToken.START_OBJECT) return JsonTokenWrapper.START_OBJECT;
        if (token == tools.jackson.core.JsonToken.START_ARRAY) return JsonTokenWrapper.START_ARRAY;
        if (token == tools.jackson.core.JsonToken.VALUE_STRING) return JsonTokenWrapper.VALUE_STRING;
        if (token == tools.jackson.core.JsonToken.VALUE_NUMBER_INT) return JsonTokenWrapper.VALUE_NUMBER_INT;
        if (token == tools.jackson.core.JsonToken.VALUE_NUMBER_FLOAT) return JsonTokenWrapper.VALUE_NUMBER_FLOAT;
        if (token == tools.jackson.core.JsonToken.VALUE_TRUE) return JsonTokenWrapper.VALUE_TRUE;
        if (token == tools.jackson.core.JsonToken.VALUE_FALSE) return JsonTokenWrapper.VALUE_FALSE;
        if (token == tools.jackson.core.JsonToken.VALUE_NULL) return JsonTokenWrapper.VALUE_NULL;
        
        return null;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Jackson3NodeWrapper) {
            return node.equals(((Jackson3NodeWrapper) other).node);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return node.hashCode();
    }
    
    @Override
    public String toString() {
        return node.toString();
    }
    
    @Override
    public Object getUnderlyingNode() {
        return node;
    }
    
    public JsonNode getJackson3Node() {
        return node;
    }
}
