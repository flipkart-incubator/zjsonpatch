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

package com.certusoft.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * A JSON patch processor that does nothing, intended for testing and validation.
 */
public class NoopProcessor implements JsonPatchProcessor {
    static final NoopProcessor INSTANCE;
    static {
        INSTANCE = new NoopProcessor();
    }

    @Override public void remove(List<String> path) {}
    @Override public void replace(List<String> path, JsonNode value) {}
    @Override public void add(List<String> path, JsonNode value) {}
    @Override public void move(List<String> fromPath, List<String> toPath) {}
    @Override public void copy(List<String> fromPath, List<String> toPath) {}
    @Override public void test(List<String> path, JsonNode value) {}
    @Override public void objectifyArrays(java.util.Map<String, List<String>> arrayKeyMap) {}
    @Override public void arrayifyObjects(java.util.Map<String, List<String>> arrayKeyMap) {}
    @Override public void stripIds() {}

}
