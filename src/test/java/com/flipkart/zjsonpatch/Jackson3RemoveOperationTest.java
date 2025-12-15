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
 * Jackson 3.x compatibility tests for Remove operations.
 * Tests the same functionality as {@link RemoveOperationTest} but using Jackson 3.x APIs.
 * 
 * @author Mariusz Sondecki
 */
class Jackson3RemoveOperationTest extends Jackson3AbstractTest {

    static Collection<Jackson3PatchTestCase> data() throws IOException {
        return Jackson3PatchTestCase.load("remove");
    }
}
