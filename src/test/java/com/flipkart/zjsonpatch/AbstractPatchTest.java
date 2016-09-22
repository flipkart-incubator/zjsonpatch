package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

@RunWith(Parameterized.class)
public abstract class AbstractPatchTest {

    private final Map<String, String> map;

    @Parameter
    public TestCase p;

    protected AbstractPatchTest() {
        this(ImmutableMap.<String, String> builder()
                .put("first", "node")
                .put("second", "expected")
                .put("patch", "op")
                .build());
    }

    protected AbstractPatchTest(Map<String, String> map) {
        this.map = map;
    }

    private JsonNode operation(JsonNode first, JsonNode patch) {
        if (p.getFlags() == null) {
            JsonPatch.validate(patch);
            return JsonPatch.apply(patch, first);
        }
        JsonPatch.validate(patch, p.getFlags());
        return JsonPatch.apply(patch, first, p.getFlags());
    }

    @Test
    public void test() throws Exception {
        if (p.isOperation()) {
            testOpertaion();
        } else {
            testError();
        }
    }

    private void testOpertaion() throws Exception {
        JsonNode node = p.getNode();

        JsonNode first = node.get(map.get("first"));
        JsonNode second = node.get(map.get("second"));
        JsonNode patch = node.get(map.get("patch"));
        String message = node.has("message") ? node.get("message").toString() : "";

        final JsonNode actualSecond = operation(first, patch);

        assertThat(message, actualSecond, equalTo(second));
    }

    private void testError() {
        JsonNode node = p.getNode();

        JsonNode first = node.get(map.get("first"));
        JsonNode patch = node.get(map.get("patch"));
        String error = node.has("error") ? node.get("error").asText() : null;

        try {
            operation(first, patch);
            fail("Failure expected: " + node.get("message"));
        } catch (Exception ex) {
            if (error != null) {
                assertThat(ex.toString(), equalTo(error));
            }
        }
    }
}
