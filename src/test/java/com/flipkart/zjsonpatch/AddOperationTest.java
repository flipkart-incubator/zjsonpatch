package com.flipkart.zjsonpatch;

import java.io.IOException;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;

public class AddOperationTest extends AbstractTest {

    @Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("add");
    }
}
