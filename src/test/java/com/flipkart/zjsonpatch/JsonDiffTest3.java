/*
 * Copyright 2016 Ravi Kalla.
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class JsonDiffTest3 {
    static ObjectMapper objectMapper = new ObjectMapper();
    static ArrayNode json1MBNode1;
    static ArrayNode json1MBNode2;
    static ArrayNode json8MBNode1;
    static ArrayNode json8MBNode2;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String strPath = "/testdata/1MBFile1.json";
        InputStream resourceAsStream = JsonDiffTest3.class.getResourceAsStream(strPath);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        json1MBNode1 = (ArrayNode) objectMapper.readTree(testData);

        strPath = "/testdata/1MBFile2.json";
        resourceAsStream = JsonDiffTest3.class.getResourceAsStream(strPath);
        testData = IOUtils.toString(resourceAsStream, "UTF-8");
        json1MBNode2 = (ArrayNode) objectMapper.readTree(testData);

        strPath = "/testdata/8MBFile1.json";
        resourceAsStream = JsonDiffTest3.class.getResourceAsStream(strPath);
        testData = IOUtils.toString(resourceAsStream, "UTF-8");
        json8MBNode1 = (ArrayNode) objectMapper.readTree(testData);

        strPath = "/testdata/8MBFile2.json";
        resourceAsStream = JsonDiffTest3.class.getResourceAsStream(strPath);
        testData = IOUtils.toString(resourceAsStream, "UTF-8");
        json8MBNode2 = (ArrayNode) objectMapper.readTree(testData);
    }

    @Test
    public void test1MBJsonDiff() throws Exception {
        JsonNode actualPatch = JsonDiff.asJson(json1MBNode1, json1MBNode2);

        System.out.println(actualPatch);

        JsonNode secondPrime = JsonPatch.apply(actualPatch, json1MBNode1);
//        System.out.println(secondPrime);
        Assert.assertTrue(json1MBNode2.equals(secondPrime));
    }
    @Test
    public void test8MBJsonDiff() throws Exception {
        JsonNode actualPatch = JsonDiff.asJson(json8MBNode1, json8MBNode2);

        System.out.println(actualPatch);

        JsonNode secondPrime = JsonPatch.apply(actualPatch, json8MBNode1);
//        System.out.println(secondPrime);
        Assert.assertTrue(json8MBNode2.equals(secondPrime));
    }
}
