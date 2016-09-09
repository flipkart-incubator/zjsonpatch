package com.flipkart.zjsonpatch;

import java.io.IOException;
import java.util.Collection;
import org.junit.runners.Parameterized;

/**
 * @author ctranxuan (streamdata.io).
 */
public class Rfc6902SamplesTest extends AbstractTest {

    @Parameterized.Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("rfc6902-samples");
    }
}
