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

interface JsonPatchProcessor {
    void remove(JsonPointer path);
    void replace(JsonPointer path, JsonNode value);
    void add(JsonPointer path, JsonNode value);
    void move(JsonPointer fromPath, JsonPointer toPath);
    void copy(JsonPointer fromPath, JsonPointer toPath);
    void test(JsonPointer path, JsonNode value);
}
