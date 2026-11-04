package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestNodesEmissionTest {

    private static ObjectMapper mapper = new ObjectMapper();

    private static EnumSet<DiffFlags> flags;

    static {
        flags = DiffFlags.defaults();
        flags.add(DiffFlags.EMIT_TEST_OPERATIONS);
    }

    @Test
    public void testNodeEmittedBeforeReplaceOperation() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":\"original\"}");
        JsonNode target = mapper.readTree("{\"key\":\"replaced\"}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode testNode = mapper.readTree("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeCopyOperation() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":\"original\"}");
        JsonNode target = mapper.readTree("{\"key\":\"original\", \"copied\":\"original\"}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode testNode = mapper.readTree("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeMoveOperation() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":\"original\"}");
        JsonNode target = mapper.readTree("{\"moved\":\"original\"}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode testNode = mapper.readTree("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveOperation() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":\"original\"}");
        JsonNode target = mapper.readTree("{}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode testNode = mapper.readTree("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromMiddleOfArray() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":[1,2,3]}");
        JsonNode target = mapper.readTree("{\"key\":[1,3]}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode testNode = mapper.readTree("{\"op\":\"test\",\"path\":\"/key/1\",\"value\":2}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromTailOfArray() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":[1,2,3]}");
        JsonNode target = mapper.readTree("{\"key\":[1,2]}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode testNode = mapper.readTree("{\"op\":\"test\",\"path\":\"/key/2\",\"value\":3}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeMoveWhenAddPrecedesRemove() throws IOException {
        JsonNode source = mapper.readTree("[2,1]");
        JsonNode target = mapper.readTree("[1,2]");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonNode expected = mapper.readTree("[" +
                "{\"op\":\"test\",\"path\":\"/1\",\"value\":1}," +
                "{\"op\":\"move\",\"from\":\"/1\",\"path\":\"/0\"}]");
        assertEquals(expected, diff);
        assertEquals(target, JsonPatch.apply(diff, source));
    }

    @Test
    public void patchWithMoveInNestedArrayAppliesToSource() throws IOException {
        JsonNode source = mapper.readTree("{\"key\":[\"a\",\"b\",\"c\",\"d\"]}");
        JsonNode target = mapper.readTree("{\"key\":[\"d\",\"a\",\"b\",\"c\"]}");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        assertEquals(target, JsonPatch.apply(diff, source));
    }

    @Test
    public void patchWithMoveInObjectValueAppliesToSource() throws IOException {
        JsonNode source = mapper.readTree("[{\"id\":1},{\"id\":2},{\"id\":3}]");
        JsonNode target = mapper.readTree("[{\"id\":3},{\"id\":1},{\"id\":2}]");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        assertEquals(target, JsonPatch.apply(diff, source));
    }

    @Test(expected = JsonPatchTestFailedException.class)
    public void patchWithMoveIsRejectedWhenMovedValueHasChanged() throws IOException {
        JsonNode source = mapper.readTree("[2,1]");
        JsonNode target = mapper.readTree("[1,2]");

        JsonNode diff = JsonDiff.asJson(source, target, flags);

        JsonPatch.apply(diff, mapper.readTree("[2,9]"));
    }
}
