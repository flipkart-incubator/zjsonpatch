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
 * Jackson 2.x specific wrapper implementations.
 * 
 * <p>This package contains concrete implementations of the mapping layer interfaces
 * specifically designed to work with Jackson 2.x (com.fasterxml.jackson.databind) APIs.
 * These classes provide the bridge between the generic wrapper interfaces and Jackson 2.x
 * native types.
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.jackson2.Jackson2NodeWrapper} - Wraps Jackson 2.x JsonNode</li>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.jackson2.Jackson2ArrayNodeWrapper} - Wraps Jackson 2.x ArrayNode</li>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.jackson2.Jackson2ObjectNodeWrapper} - Wraps Jackson 2.x ObjectNode</li>
 *   <li>{@link com.flipkart.zjsonpatch.mapping.jackson2.Jackson2NodeFactory} - Wraps Jackson 2.x JsonNodeFactory</li>
 * </ul>
 * 
 * <h2>Dependencies</h2>
 * 
 * <p>This package requires Jackson 2.x libraries to be present on the classpath:
 * <ul>
 *   <li>com.fasterxml.jackson.core:jackson-core</li>
 *   <li>com.fasterxml.jackson.core:jackson-databind</li>
 * </ul>
 * 
 * <p>These implementations are automatically selected when the actual runtime node
 * is an instance of {@code com.fasterxml.jackson.databind.JsonNode} (Jackson 2.x).
 * Selection is based on the node type itself, not on classpath availability or
 * system properties. When both Jackson 2.x and 3.x are present, the appropriate
 * wrapper is chosen based on the specific node instance being processed.
 * 
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
package com.flipkart.zjsonpatch.mapping.jackson2;
