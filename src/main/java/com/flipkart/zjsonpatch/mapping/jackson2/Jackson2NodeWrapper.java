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

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.mapping.*;

import java.util.Iterator;

/**
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class Jackson2NodeWrapper implements JsonNodeWrapper {
    
    private final JsonNode node;
    
    public Jackson2NodeWrapper(JsonNode node) {
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
        return node.isTextual();
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
        return node.isContainerNode();
    }
    
    @Override
    public JsonNodeWrapper get(String fieldName) {
        JsonNode result = node.get(fieldName);
        return result != null ? new Jackson2NodeWrapper(result) : null;
    }
    
    @Override
    public JsonNodeWrapper get(int index) {
        JsonNode result = node.get(index);
        return result != null ? new Jackson2NodeWrapper(result) : null;
    }
    
    @Override
    public boolean has(String fieldName) {
        return node.has(fieldName);
    }
    
    @Override
    public String textValue() {
        return node.textValue();
    }
    
    
    @Override
    public int size() {
        return node.size();
    }
    
    @Override
    public Iterator<String> fieldNames() {
        return node.fieldNames();
    }

    @Override
    public Iterator<JsonNodeWrapper> iterator() {
        return new IteratorDecorator<>(node.iterator());
    }
    
    @Override
    public ArrayNodeWrapper arrayValue() {
        if (node.isArray()) {
            return new Jackson2ArrayNodeWrapper((ArrayNode) node);
        }
        return null;
    }
    
    @Override
    public ObjectNodeWrapper objectValue() {
        if (node.isObject()) {
            return new Jackson2ObjectNodeWrapper((ObjectNode) node);
        }
        return null;
    }
    
    @Override
    public JsonNodeWrapper deepCopy() {
        return new Jackson2NodeWrapper(node.deepCopy());
    }
    
    @Override
    public JsonTokenWrapper asToken() {
        return mapToken(node.asToken());
    }
    
    private JsonTokenWrapper mapToken(JsonToken token) {
        if (token == null) return null;
        return switch (token) {
            case START_OBJECT -> JsonTokenWrapper.START_OBJECT;
            case START_ARRAY -> JsonTokenWrapper.START_ARRAY;
            case VALUE_STRING -> JsonTokenWrapper.VALUE_STRING;
            case VALUE_NUMBER_INT -> JsonTokenWrapper.VALUE_NUMBER_INT;
            case VALUE_NUMBER_FLOAT -> JsonTokenWrapper.VALUE_NUMBER_FLOAT;
            case VALUE_TRUE -> JsonTokenWrapper.VALUE_TRUE;
            case VALUE_FALSE -> JsonTokenWrapper.VALUE_FALSE;
            case VALUE_NULL -> JsonTokenWrapper.VALUE_NULL;
            default -> null;
        };
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Jackson2NodeWrapper) {
            return node.equals(((Jackson2NodeWrapper) other).node);
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
    
    public JsonNode getJackson2Node() {
        return node;
    }
}
