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

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author ctranxuan (streamdata.io).
 */
public abstract class AbstractOperationTest {
    static ObjectMapper objectMapper = new ObjectMapper();
    static ArrayNode jsonNode;
    static ArrayNode errorNode;

    protected AbstractOperationTest(String operationName) throws IOException {
        String path = "/testdata/" + operationName + ".json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        JsonNode testsNode = objectMapper.readTree(testData);
        jsonNode = (ArrayNode) testsNode.get("ops");
        errorNode = (ArrayNode) testsNode.get("errors");
    }

    @Test
    public void testPatchAppliedCleanly() throws Exception {
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode first = jsonNode.get(i).get("node");
            JsonNode second = jsonNode.get(i).get("expected");
            JsonNode patch = jsonNode.get(i).get("op");

            System.out.println("Test # " + i);
            System.out.println(first);
            System.out.println(second);
            System.out.println(patch);

            JsonNode secondPrime = JsonPatch.apply(patch, first);
            System.out.println(secondPrime);
            Assert.assertThat(secondPrime, equalTo(second));
        }
    }

    @Test(expected = RuntimeException.class)
    public void testErrorsAreCorrectlyReported() {
        for (int i = 0; i < errorNode.size(); i++) {
            JsonNode first = errorNode.get(i).get("node");
            JsonNode patch = errorNode.get(i).get("op");

            System.out.println("Error Test # " + i);
            System.out.println(first);
            System.out.println(patch);

            JsonNode secondPrime = JsonPatch.apply(patch, first);
            System.out.println(secondPrime);
        }

        if (errorNode.size() == 0) {
            throw new RuntimeException("dummy exception");
        }
    }

}
