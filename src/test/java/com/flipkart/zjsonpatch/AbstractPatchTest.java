package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public abstract class AbstractPatchTest {

    private final boolean rfc;

    @Parameter
    public TestCase p;

    protected AbstractPatchTest() {
        this(true);
    }

    protected AbstractPatchTest(boolean rfc) {
        this.rfc = rfc;
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

        JsonNode first = node.get(rfc ? "node" : "first");
        JsonNode second = node.get(rfc ? "expected" : "second");
        JsonNode patch = node.get(rfc ? "op" : "patch");
        String message = node.has("message") ? node.get("message").toString() : "";

        final JsonNode actualSecond = operation(first, patch);

        assertThat(message, actualSecond, equalTo(second));
    }

    private void testError() {
        JsonNode node = p.getNode();

        JsonNode first = node.get(rfc ? "node" : "first");
        JsonNode patch = node.get(rfc ? "op" : "patch");
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
