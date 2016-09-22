package com.flipkart.zjsonpatch;

import java.io.IOException;
import java.util.Collection;

import org.junit.runners.Parameterized;

import com.google.common.collect.ImmutableMap;

/**
 * JSON Diff test.
 */
public class JsonPatchTest extends AbstractPatchTest {

    public JsonPatchTest() {
        super(ImmutableMap.<String, String> builder()
                .put("first", "first")
                .put("second", "second")
                .put("patch", "patch")
                .build());
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<TestCase> data() throws IOException {
        return TestCase.load("patch");
    }
}
