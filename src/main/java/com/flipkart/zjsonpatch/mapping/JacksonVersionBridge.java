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

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridge for wrapping and unwrapping Jackson JsonNodes across different versions.
 * <p>
 * This class handles the bridging responsibility - converting between
 * native Jackson nodes and wrapped nodes. It automatically detects the Jackson
 * version of nodes by checking if they are instances of known Jackson JsonNode types,
 * properly handling inheritance and subclassed nodes.
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public final class JacksonVersionBridge {
    
    private static final ConcurrentHashMap<String, Class<?>> WRAPPER_CLASS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    private static final String JACKSON2_NODE_WRAPPER = "com.flipkart.zjsonpatch.mapping.jackson2.Jackson2NodeWrapper";
    private static final String JACKSON2_ARRAY_WRAPPER = "com.flipkart.zjsonpatch.mapping.jackson2.Jackson2ArrayNodeWrapper";
    private static final String JACKSON2_OBJECT_WRAPPER = "com.flipkart.zjsonpatch.mapping.jackson2.Jackson2ObjectNodeWrapper";
    private static final String JACKSON3_NODE_WRAPPER = "com.flipkart.zjsonpatch.mapping.jackson3.Jackson3NodeWrapper";
    private static final String JACKSON3_ARRAY_WRAPPER = "com.flipkart.zjsonpatch.mapping.jackson3.Jackson3ArrayNodeWrapper";
    private static final String JACKSON3_OBJECT_WRAPPER = "com.flipkart.zjsonpatch.mapping.jackson3.Jackson3ObjectNodeWrapper";
    
    private static final String JACKSON2_JSON_NODE = "com.fasterxml.jackson.databind.JsonNode";
    private static final String JACKSON2_ARRAY_NODE = "com.fasterxml.jackson.databind.node.ArrayNode";
    private static final String JACKSON2_OBJECT_NODE = "com.fasterxml.jackson.databind.node.ObjectNode";
    private static final String JACKSON3_JSON_NODE = "tools.jackson.databind.JsonNode";
    private static final String JACKSON3_ARRAY_NODE = "tools.jackson.databind.node.ArrayNode";
    private static final String JACKSON3_OBJECT_NODE = "tools.jackson.databind.node.ObjectNode";

    private JacksonVersionBridge() {}

    /**
     * Wraps a native Jackson JsonNode in the appropriate JsonNodeWrapper.
     * <p>
     *
     * @param nativeNode the Jackson JsonNode to wrap
     * @return wrapped JsonNode, or null if input is null
     * @throws IllegalArgumentException if the node type is unsupported for the selected Jackson version
     * @throws JacksonVersionException if wrapper classes cannot be loaded or instantiated
     */
    public static JsonNodeWrapper wrap(Object nativeNode) {
        if (nativeNode == null) return null;

        try {
            NodeTypeResolver resolver = selectResolverFor(nativeNode);
            return resolver.wrapNode(nativeNode);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new JacksonVersionException(
                "Failed to wrap Jackson node: " + nativeNode.getClass().getName(), e);
        }
    }

    /**
     * Selects the appropriate NodeTypeResolver based on the actual node type.
     * This method detects whether a node is from Jackson 2.x or 3.x by
     * checking if the node is an instance of known Jackson node types,
     * properly handling subclassed nodes.
     *
     * @param nativeNode the node to inspect
     * @return the appropriate NodeTypeResolver
     * @throws IllegalArgumentException if the node type is unknown
     */
    private static NodeTypeResolver selectResolverFor(Object nativeNode) {
        if (isInstanceOf(nativeNode, JACKSON2_JSON_NODE)) {
            return NodeTypeResolver.JACKSON_2;
        }

        if (isInstanceOf(nativeNode, JACKSON3_JSON_NODE)) {
            return NodeTypeResolver.JACKSON_3;
        }

        throw new IllegalArgumentException(
            "Unknown Jackson node type: %s. Expected node extending com.fasterxml.jackson.databind.JsonNode (Jackson 2.x) or tools.jackson.databind.JsonNode (Jackson 3.x)"
                .formatted(nativeNode.getClass().getName())
        );
    }


    /**
     * Creates a wrapper instance.
     * 
     * @param wrapperClassName the fully qualified wrapper class name
     * @param nativeNode the native Jackson node to wrap
     * @return the wrapper instance
     */
    private static JsonNodeWrapper createWrapper(String wrapperClassName, Object nativeNode) throws Exception {
        Constructor<?> constructor = getConstructor(wrapperClassName);
        return (JsonNodeWrapper) constructor.newInstance(nativeNode);
    }
    
    /**
     * Gets a constructor for the wrapper class.
     * Determines the correct parameter type based on the wrapper class name.
     * 
     * @param wrapperClassName the wrapper class name
     * @return the constructor
     */
    private static Constructor<?> getConstructor(String wrapperClassName) {
        return CONSTRUCTOR_CACHE.computeIfAbsent(wrapperClassName, className -> {
            try {
                Class<?> wrapperClass = getWrapperClass(className);
                Class<?> parameterType = determineParameterType(className);
                return wrapperClass.getConstructor(parameterType);
            } catch (Exception e) {
                throw new JacksonVersionException(
                    "Failed to get constructor for wrapper class: %s".formatted(className), e);
            }
        });
    }

    private static Class<?> determineParameterType(String className) throws ClassNotFoundException {
        return switch (getWrapperType(className)) {
            case JACKSON2_ARRAY -> Class.forName(JACKSON2_ARRAY_NODE);
            case JACKSON2_OBJECT -> Class.forName(JACKSON2_OBJECT_NODE);
            case JACKSON2_NODE -> Class.forName(JACKSON2_JSON_NODE);
            case JACKSON3_ARRAY -> Class.forName(JACKSON3_ARRAY_NODE);
            case JACKSON3_OBJECT -> Class.forName(JACKSON3_OBJECT_NODE);
            case JACKSON3_NODE -> Class.forName(JACKSON3_JSON_NODE);
        };
    }

    private static WrapperType getWrapperType(String className) {
        boolean isJackson2 = className.contains("jackson2");
        boolean isJackson3 = className.contains("jackson3");
        boolean isArray = className.contains("ArrayNodeWrapper");
        boolean isObject = className.contains("ObjectNodeWrapper");
        
        if (isJackson2 && isArray) return WrapperType.JACKSON2_ARRAY;
        if (isJackson2 && isObject) return WrapperType.JACKSON2_OBJECT;  
        if (isJackson2) return WrapperType.JACKSON2_NODE;
        
        if (isJackson3 && isArray) return WrapperType.JACKSON3_ARRAY;
        if (isJackson3 && isObject) return WrapperType.JACKSON3_OBJECT;
        if (isJackson3) return WrapperType.JACKSON3_NODE;
        
        throw new IllegalArgumentException("Unknown wrapper type: " + className);
    }

    /**
     * Gets a wrapper class.
     * 
     * @param wrapperClassName the wrapper class name
     * @return the wrapper class
     */
    private static Class<?> getWrapperClass(String wrapperClassName) {
        return WRAPPER_CLASS_CACHE.computeIfAbsent(wrapperClassName, className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JacksonVersionException(
                    "Wrapper class not found: %s. Ensure the corresponding Jackson version is available."
                        .formatted(className), e);
            }
        });
    }
    
    /**
     * Checks if an object is an instance of the specified class name.
     * 
     * @param obj the object to check
     * @param className the class name to check against
     * @return true if the object is an instance of the class, false otherwise
     */
    private static boolean isInstanceOf(Object obj, String className) {
        Class<?> clazz = CLASS_CACHE.computeIfAbsent(className, k -> {
            try {
                return Class.forName(k);
            } catch (ClassNotFoundException e) {
                return void.class;
            }
        });
        return clazz.isInstance(obj);
    }    
    /**
     * Unwraps a JsonNodeWrapper to its underlying Jackson JsonNode.
     * 
     * @param wrapper the wrapper to unwrap
     * @return the underlying Jackson JsonNode, or null if wrapper is null
     */
    public static <T> T unwrap(JsonNodeWrapper wrapper) {
        if (wrapper == null) return null;
        return (T) wrapper.getUnderlyingNode();
    }

    private enum NodeTypeResolver {
        JACKSON_2(
                new NodeTypeMapping(JACKSON2_ARRAY_NODE, JACKSON2_ARRAY_WRAPPER),
                new NodeTypeMapping(JACKSON2_OBJECT_NODE, JACKSON2_OBJECT_WRAPPER),
                new NodeTypeMapping(JACKSON2_JSON_NODE, JACKSON2_NODE_WRAPPER)
        ),
        JACKSON_3(
                new NodeTypeMapping(JACKSON3_ARRAY_NODE, JACKSON3_ARRAY_WRAPPER),
                new NodeTypeMapping(JACKSON3_OBJECT_NODE, JACKSON3_OBJECT_WRAPPER),
                new NodeTypeMapping(JACKSON3_JSON_NODE, JACKSON3_NODE_WRAPPER)
        );

        private final NodeTypeMapping[] mappings;

        NodeTypeResolver(NodeTypeMapping... mappings) {
            this.mappings = mappings;
        }

        JsonNodeWrapper wrapNode(Object nativeNode) throws Exception {
            for (var mapping : mappings) {
                if (isInstanceOf(nativeNode, mapping.nodeType())) {
                    return createWrapper(mapping.wrapperClass(), nativeNode);
                }
            }
            throw new IllegalArgumentException("Unsupported Jackson node type: %s"
                    .formatted(nativeNode.getClass()));
        }
    }

    private record NodeTypeMapping(String nodeType, String wrapperClass) {}

    private enum WrapperType {
        JACKSON2_ARRAY, JACKSON2_OBJECT, JACKSON2_NODE,
        JACKSON3_ARRAY, JACKSON3_OBJECT, JACKSON3_NODE
    }

}
