package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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
}
