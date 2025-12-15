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

package com.flipkart.zjsonpatch.mapping;

import java.util.Iterator;

/**
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public interface JsonNodeWrapper extends Iterable<JsonNodeWrapper> {
    
    boolean isArray();
    boolean isObject();
    boolean isTextual();
    boolean isNumber();
    boolean isBoolean();
    boolean isNull();
    boolean isContainerNode();
    
    JsonNodeWrapper get(String fieldName);
    JsonNodeWrapper get(int index);
    boolean has(String fieldName);
    
    String textValue();

    int size();
    Iterator<String> fieldNames();
    Iterator<JsonNodeWrapper> iterator();

    ArrayNodeWrapper arrayValue();
    ObjectNodeWrapper objectValue();
    
    JsonNodeWrapper deepCopy();
    
    JsonTokenWrapper asToken();
    
    boolean equals(Object other);
    
    String toString();
    
    Object getUnderlyingNode();
}
