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
import java.util.Random;

/**
 * Unit test
 */
public class JsonDiffTest {
    static ObjectMapper objectMapper = new ObjectMapper();
    static ArrayNode jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/sample.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = (ArrayNode) objectMapper.readTree(testData);
    }

    @Test
    public void testSampleJsonDiff() throws Exception {
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode first = jsonNode.get(i).get("first");
            JsonNode second = jsonNode.get(i).get("second");

            JsonNode actualPatch = JsonDiff.asJson(first, second);

            System.out.println("Test # " + i);
            System.out.println(first);
            System.out.println(second);
            System.out.println(actualPatch);

            JsonNode secondPrime = JsonPatch.apply(actualPatch, first);
            System.out.println(secondPrime);
            Assert.assertTrue(second.equals(secondPrime));
        }
    }

    @Test
    public void testGeneratedJsonDiff() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            JsonNode first = TestDataGenerator.generate(random.nextInt(10));
            JsonNode second = TestDataGenerator.generate(random.nextInt(10));

            JsonNode actualPatch = JsonDiff.asJson(first, second);
            System.out.println("Test # " + i);

            System.out.println(first);
            System.out.println(second);
            System.out.println(actualPatch);

            JsonNode secondPrime = JsonPatch.apply(actualPatch, first);
            System.out.println(secondPrime);
            Assert.assertTrue(second.equals(secondPrime));
        }
    }
}
