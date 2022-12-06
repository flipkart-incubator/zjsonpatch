/*
 * Copyright 2021 flipkart.com zjsonpatch.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * ATTRIBUTION NOTICE:<br>
 * This class contains source code copied from <a href="https://github.com/apache/commons-collections/tree/9414e73a7b8c5434b7cfcc5a65fc9baa007a1861">
 * Apache commons-collection4
 * <a>
 * </p>
 */
public class TestUtils {

    public static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    private TestUtils() {
    }


    public static JsonNode loadResourceAsJsonNode(String path) throws IOException {
        String testData = loadFromResources(path);
        return DEFAULT_MAPPER.readTree(testData);
    }

    public static String loadFromResources(String path) throws IOException {
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }
    
    public void testLongestCommonSubsequence() {

        try {
            InternalUtils.longestCommonSubsequence((List<?>) null, null);
            fail("failed to check for null argument");
        } catch (final NullPointerException e) {}

        try {
            InternalUtils.longestCommonSubsequence(Arrays.asList('A'), null);
            fail("failed to check for null argument");
        } catch (final NullPointerException e) {}

        try {
            InternalUtils.longestCommonSubsequence(null, Arrays.asList('A'));
            fail("failed to check for null argument");
        } catch (final NullPointerException e) {}

        @SuppressWarnings("unchecked")
        List<Character> lcs = InternalUtils.longestCommonSubsequence(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        assertEquals(0, lcs.size());

        final List<Character> list1 = Arrays.asList('B', 'A', 'N', 'A', 'N', 'A');
        final List<Character> list2 = Arrays.asList('A', 'N', 'A', 'N', 'A', 'S');
        lcs = InternalUtils.longestCommonSubsequence(list1, list2);

        List<Character> expected = Arrays.asList('A', 'N', 'A', 'N', 'A');
        assertEquals(expected, lcs);

        final List<Character> list3 = Arrays.asList('A', 'T', 'A', 'N', 'A');
        lcs = InternalUtils.longestCommonSubsequence(list1, list3);

        expected = Arrays.asList('A', 'A', 'N', 'A');
        assertEquals(expected, lcs);

        final List<Character> listZorro = Arrays.asList('Z', 'O', 'R', 'R', 'O');
        lcs = InternalUtils.longestCommonSubsequence(list1, listZorro);

        assertTrue(lcs.isEmpty());
    }
}
