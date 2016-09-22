package com.flipkart.zjsonpatch;

import java.io.IOException;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.runners.Parameterized;

/**
 * @author ctranxuan (streamdata.io).
 *
 * These tests comes from JS JSON-Patch libraries (
 * https://github.com/Starcounter-Jack/JSON-Patch/blob/master/test/spec/json-patch-tests/tests.json
 * https://github.com/cujojs/jiff/blob/master/test/json-patch-tests/tests.json)
 */
// @Ignore
public class JsLibSamplesTest extends AbstractPatchTest {

    @Parameterized.Parameters
    public static Collection<TestCase> data() throws IOException {
        return TestCase.load("js-libs-samples");
    }
}
