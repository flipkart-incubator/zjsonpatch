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
 * Mapping layer for Jackson version compatibility.
 * 
 * <p>This package provides an abstraction layer that enables zjsonpatch to work seamlessly
 * with both Jackson 2.x and Jackson 3.x libraries. The mapping layer uses a wrapper pattern
 * to provide a unified interface for JSON operations while maintaining compatibility with
 * different Jackson versions that may be present on the classpath.
 * 
 * <h2>Architecture Overview</h2>
 * 
 * <p>The mapping layer consists of several key components:
 * 
 * <ul>
 *   <li><b>Wrapper Interfaces</b> - Define common operations for JSON nodes, arrays, and objects</li>
 *   <li><b>Version Bridge</b> - Handles conversion between native Jackson types and wrappers</li>
 *   <li><b>Version Selector</b> - Automatically detects and selects appropriate Jackson version</li>
 *   <li><b>Factory Provider</b> - Provides version-appropriate JSON node factories</li>
 * </ul>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.JsonNodeWrapper} - Base wrapper for all JSON nodes</li>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.ArrayNodeWrapper} - Wrapper for JSON array operations</li>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.ObjectNodeWrapper} - Wrapper for JSON object operations</li>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.JsonNodeFactoryWrapper} - Wrapper for JSON node factory operations</li>
 * </ul>
 * 
 * <h2>Version Management</h2>
 *
 * <ul>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.JacksonVersionBridge} - Converts between wrapper and native types and auto-detects Jackson versions</li>
 * </ul>
 * 
 * <h2>Implementation Packages</h2>
 * 
 * <ul>
 *   <li>{@code com.flipkart.zjsonpatch.mapping.jackson2} - Jackson 2.x specific implementations</li>
 *   <li>{@code com.flipkart.zjsonpatch.mapping.jackson3} - Jackson 3.x specific implementations</li>
 * </ul>
 * 
 * <h2>Version Detection</h2>
 *
 * <p>The mapping layer automatically detects the Jackson version from the actual node types
 * by checking if they are instances of known Jackson JsonNode types:
 * <ul>
 *   <li>Jackson 2.x nodes: instances of {@code com.fasterxml.jackson.databind.JsonNode}</li>
 *   <li>Jackson 3.x nodes: instances of {@code tools.jackson.databind.JsonNode}</li>
 * </ul>
 *
 * <p>This instanceof-based approach properly handles inheritance, including user-defined subclasses
 * of Jackson nodes. It ensures that each API (JsonDiff/Jackson3JsonDiff and JsonPatch/Jackson3JsonPatch)
 * works correctly with its intended Jackson version, regardless of what versions are available
 * on the classpath.
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Create JSON nodes using either Jackson 2.x or 3.x ObjectMapper
 * JsonNode source = mapper.readTree("{\"foo\":\"bar\"}");
 * JsonNode target = mapper.readTree("{\"foo\":\"baz\"}");
 * 
 * // For Jackson 2.x, use JsonDiff and JsonPatch:
 * JsonNode patch = JsonDiff.asJson(source, target);
 * JsonNode result = JsonPatch.apply(patch, source);
 *
 * // For Jackson 3.x, use the Jackson3-prefixed classes:
 * JsonNode patch = Jackson3JsonDiff.asJson(source, target);
 * JsonNode result = Jackson3JsonPatch.apply(patch, source);
 * }</pre>
 *
 * <h2>Performance Considerations</h2>
 * 
 * <p>The wrapper layer introduces minimal overhead as it primarily delegates to the
 * underlying Jackson implementations. Version detection is cached to avoid repeated
 * Class.forName() lookups. The detection gracefully handles missing Jackson versions
 * via exception handling in the isInstanceOf() method.
 * 
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
package com.flipkart.zjsonpatch.mapping;
