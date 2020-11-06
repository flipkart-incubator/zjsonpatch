/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.flipkart.zjsonpatch;

import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

/**
 * @author ctranxuan (streamdata.io).
 */
public class Rfc6902SamplesTest extends AbstractTest {

    @Parameterized.Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("rfc6902-samples");
    }

    @Override
    protected boolean matchOnErrors() {
        // Error matching disabled to avoid a lot of rote work on the samples.
        // TODO revisit samples and possibly change "message" fields to "reference" or something more descriptive
        return false;
    }
}
