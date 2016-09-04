package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author ctranxuan (streamdata.io).
 */
public abstract class AbstractTest {
    ObjectMapper objectMapper = new ObjectMapper();
    ArrayNode jsonNode;
    ArrayNode errorNode;

    protected AbstractTest(String fileName) throws IOException {
        String path = "/testdata/" + fileName + ".json";
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
            String message = jsonNode.get(i).has("message") ? jsonNode.get(i).get("message").toString() : "";

            System.out.println("Test # " + i);
            System.out.println(first);
            System.out.println(second);
            System.out.println(patch);

            JsonNode secondPrime = JsonPatch.apply(patch, first);
            System.out.println(secondPrime);
            Assert.assertThat(message, secondPrime, equalTo(second));
        }
    }

//    @Test
//    public void testPatchSyntax() throws Exception {
//        for (int i = 0; i < jsonNode.size(); i++) {
//            JsonNode first = jsonNode.get(i).get("node");
//            JsonNode second = jsonNode.get(i).get("expected");
//            JsonNode patch = jsonNode.get(i).get("op");
//
//            System.out.println("Test # " + i);
//            System.out.println(first);
//            System.out.println(second);
//            System.out.println(patch);
//
//            JsonNode diff = JsonDiff.asJson(first, second);
//            System.out.println(diff);
//            Assert.assertThat(diff, equalTo(patch));
//        }
//    }

    @Test(expected = JsonPatchApplicationException.class)
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
            throw new JsonPatchApplicationException("dummy exception");
        }
    }

}
