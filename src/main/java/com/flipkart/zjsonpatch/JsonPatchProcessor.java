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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Applies patch operations to a JSON document.
 */
interface JsonPatchProcessor {
  
    /**
     * Removes a document node at a given path.
     * @param path Path to document node to remove.
     */
    void remove(List<String> path);
    
    /**
     * Replaces a document node at a given path.
     * @param path Path to document node to replace.
     * @param value Replacement document node.
     */
    void replace(List<String> path, JsonNode value);
    
    /**
     * Adds a value to an object or inserts it into an array at a give path
     * In the case of an array, the value is inserted before the given index. 
     * The {@literal -} character can be used instead of an index to insert at 
     * the end of an array.
     * 
     * @param path Path to document location where {@code value} is to be added.
     * @param value 
     */
    void add(List<String> path, JsonNode value);
    
    /**
     * Moves a document node at a given path to another location.
     * @param fromPath Source path to document node to move.
     * @param toPath Path to document location where node will moved to.
     */
    void move(List<String> fromPath, List<String> toPath);
    
    /**
     * Copies a document node at a given path to another location.
     * @param fromPath Source path to document node to copy.
     * @param toPath Path to document location where node will be copied to.
     */
    void copy(List<String> fromPath, List<String> toPath);
    
    /**
     * Tests if the document node at a given path is equal to a specified value.
     * @param path Path to document node to test the existence of.
     * @param value value to test if equivalent to node at specified document {@code path}.
     */
    void test(List<String> path, JsonNode value);
}
