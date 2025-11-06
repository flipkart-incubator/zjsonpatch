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

/**
 * Core zjsonpatch API for JSON Patch operations according to RFC 6902.
 * 
 * <p>This package provides the main public APIs for creating and applying JSON patches.
 * The library supports both Jackson 2.x and Jackson 3.x with separate API entry points.
 * 
 * <h2>API Selection</h2>
 * 
 * <p><b>Users must explicitly use the correct public API</b> based on their Jackson version:
 * <ul>
 *   <li>{@link com.flipkart.zjsonpatch.JsonDiff} and {@link com.flipkart.zjsonpatch.JsonPatch} for Jackson 2.x</li>
 *   <li>{@link com.flipkart.zjsonpatch.Jackson3JsonDiff} and {@link com.flipkart.zjsonpatch.Jackson3JsonPatch} for Jackson 3.x</li>
 * </ul>
 * 
 * <p>Separate APIs are necessary because:
 * <ol>
 *   <li>The original API is based on Jackson 2.x types and cannot be changed without breaking compatibility</li>
 *   <li>A unified API is not possible since only one Jackson version may be available in the classpath with incompatible package structures</li>
 * </ol>
 * 
 * <h2>Basic Usage</h2>
 * 
 * <p>For Jackson 2.x:
 * <pre>{@code
 * // Create JSON nodes using Jackson 2.x ObjectMapper
 * JsonNode source = mapper.readTree("{\"foo\":\"bar\"}");
 * JsonNode target = mapper.readTree("{\"foo\":\"baz\"}");
 * 
 * // Generate and apply patch
 * JsonNode patch = JsonDiff.asJson(source, target);
 * JsonNode result = JsonPatch.apply(patch, source);
 * }</pre>
 * 
 * <p>For Jackson 3.x:
 * <pre>{@code
 * // Create JSON nodes using Jackson 3.x ObjectMapper
 * JsonNode source = mapper.readTree("{\"foo\":\"bar\"}");
 * JsonNode target = mapper.readTree("{\"foo\":\"baz\"}");
 * 
 * // Generate and apply patch
 * JsonNode patch = Jackson3JsonDiff.asJson(source, target);
 * JsonNode result = Jackson3JsonPatch.apply(patch, source);
 * }</pre>
 * 
 * @see com.flipkart.zjsonpatch.JsonDiff
 * @see com.flipkart.zjsonpatch.JsonPatch
 * @see com.flipkart.zjsonpatch.Jackson3JsonDiff
 * @see com.flipkart.zjsonpatch.Jackson3JsonPatch
 */
package com.flipkart.zjsonpatch;