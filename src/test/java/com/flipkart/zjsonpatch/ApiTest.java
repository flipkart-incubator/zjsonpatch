package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

/**
 * User: holograph
 * Date: 03/08/16
 */
public class ApiTest {
    @Test(expected = InvalidJsonPatchException.class)
    public void applyingNonArrayPatchShouldThrowAnException() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode invalid = objectMapper.readTree("{\"not\": \"a patch\"}");
        JsonNode to = objectMapper.readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAnInvalidArrayShouldThrowAnException() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode invalid = objectMapper.readTree("[1, 2, 3, 4, 5]");
        JsonNode to = objectMapper.readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }
}
