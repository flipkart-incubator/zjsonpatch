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

import java.io.IOException;
import java.util.Collection;

/**
 * Jackson 3.x compatibility tests for RFC 6902 samples.
 * Tests the same functionality as {@link Rfc6902SamplesTest} but using Jackson 3.x APIs.
 *
 * @author Mariusz Sondecki
 */
class Jackson3Rfc6902SamplesTest extends Jackson3AbstractTest {

    static Collection<Jackson3PatchTestCase> data() throws IOException {
        return Jackson3PatchTestCase.load("rfc6902-samples");
    }

    @Override
    protected boolean matchOnErrors() {
        // Error matching disabled to avoid a lot of rote work on the samples.
        // TODO revisit samples and possibly change "message" fields to "reference" or something more descriptive
        return false;
    }
}
