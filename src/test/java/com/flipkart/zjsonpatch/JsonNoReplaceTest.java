package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;

import java.util.EnumSet;

public class JsonNoReplaceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testJsonDiffAppliesAddAndRemoveOperationExplicitly() throws JsonProcessingException {
        String a = "{\n" +
                "    \"fsnIds\": [\n" +
                "        \"F1\",\n" +
                "        \"F3\"\n" +
                "    ]\n" +
                "}";
        ObjectReader reader = OBJECT_MAPPER.reader();
        JsonNode before = reader.readTree(a);

        String b = "{\n" +
                "    \"fsnIds\": [\n" +
                "        \"F1\",\n" +
                "        \"F4\",\n" +
                "        \"F6\"\n" +
                "    ]\n" +
                "}";
        ObjectReader reader2 = OBJECT_MAPPER.reader();
        JsonNode after = reader2.readTree(b);

        System.out.println(JsonDiff.asJson(before, after, EnumSet.of(
                DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE,
                DiffFlags.ADD_EXPLICIT_ADD_REMOVE_ON_REPLACE,
                DiffFlags.OMIT_MOVE_OPERATION
        )));
    }
}
