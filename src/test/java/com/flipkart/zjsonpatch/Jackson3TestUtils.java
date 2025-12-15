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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Jackson 3.x version of {@link TestUtils} for loading test resources.
 * 
 * @author Mariusz Sondecki
 */
final class Jackson3TestUtils {

    public static final ObjectMapper DEFAULT_MAPPER = JsonMapper.builder().build();

    private Jackson3TestUtils() {
    }

    static JsonNode loadResourceAsJsonNode(String path) throws IOException {
        String testData = loadFromResources(path);
        return DEFAULT_MAPPER.readTree(testData);
    }

    static String loadFromResources(String path) throws IOException {
        try (InputStream resourceAsStream = Jackson3TestUtils.class.getResourceAsStream(path)) {
            if (resourceAsStream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        }
    }
}
