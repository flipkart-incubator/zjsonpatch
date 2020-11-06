package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @author isopropylcyanide
 */
public class JsonSplitReplaceOpTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testJsonDiffSplitsReplaceIntoAddAndRemoveOperationWhenFlagIsAdded() throws IOException {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        JsonNode sourceNode = OBJECT_MAPPER.reader().readTree(source);
        JsonNode targetNode = OBJECT_MAPPER.reader().readTree(target);

        JsonNode diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(3, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).get("op").textValue());
        assertEquals("/ids/1", diff.get(0).get("path").textValue());
        assertEquals("F3", diff.get(0).get("value").textValue());

        assertEquals(Operation.ADD.rfcName(), diff.get(1).get("op").textValue());
        assertEquals("/ids/1", diff.get(1).get("path").textValue());
        assertEquals("F6", diff.get(1).get("value").textValue());

        assertEquals(Operation.ADD.rfcName(), diff.get(2).get("op").textValue());
        assertEquals("/ids/2", diff.get(2).get("path").textValue());
        assertEquals("F4", diff.get(2).get("value").textValue());
    }

    @Test
    public void testJsonDiffDoesNotSplitReplaceIntoAddAndRemoveOperationWhenFlagIsNotAdded() throws IOException {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        JsonNode sourceNode = OBJECT_MAPPER.reader().readTree(source);
        JsonNode targetNode = OBJECT_MAPPER.reader().readTree(target);

        JsonNode diff = JsonDiff.asJson(sourceNode, targetNode);
        System.out.println(diff);
        assertEquals(2, diff.size());
        assertEquals(Operation.REPLACE.rfcName(), diff.get(0).get("op").textValue());
        assertEquals("/ids/1", diff.get(0).get("path").textValue());
        assertEquals("F6", diff.get(0).get("value").textValue());

        assertEquals(Operation.ADD.rfcName(), diff.get(1).get("op").textValue());
        assertEquals("/ids/2", diff.get(1).get("path").textValue());
        assertEquals("F4", diff.get(1).get("value").textValue());
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyRemove() throws IOException {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F3\"] }";

        JsonNode sourceNode = OBJECT_MAPPER.reader().readTree(source);
        JsonNode targetNode = OBJECT_MAPPER.reader().readTree(target);

        JsonNode diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).get("op").textValue());
        assertEquals("/ids/0", diff.get(0).get("path").textValue());
        assertEquals("F1", diff.get(0).get("value").textValue());
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyAdd() throws IOException {
        String source = "{ \"ids\": [ \"F1\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\"] }";

        JsonNode sourceNode = OBJECT_MAPPER.reader().readTree(source);
        JsonNode targetNode = OBJECT_MAPPER.reader().readTree(target);

        JsonNode diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.ADD.rfcName(), diff.get(0).get("op").textValue());
        assertEquals("/ids/1", diff.get(0).get("path").textValue());
        assertEquals("F6", diff.get(0).get("value").textValue());
    }
}
