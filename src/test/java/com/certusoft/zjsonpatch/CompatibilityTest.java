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

package com.certusoft.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;

import static com.certusoft.zjsonpatch.CompatibilityFlags.MISSING_VALUES_AS_NULLS;
import static com.certusoft.zjsonpatch.CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CompatibilityTest {

    ObjectMapper mapper;
    JsonNode addNodeWithMissingValue;
    JsonNode replaceNodeWithMissingValue;
    JsonNode removeNoneExistingArrayElement;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        addNodeWithMissingValue = mapper.readTree("[{\"op\":\"add\",\"path\":\"a\"}]");
        replaceNodeWithMissingValue = mapper.readTree("[{\"op\":\"replace\",\"path\":\"a\"}]");
        removeNoneExistingArrayElement = mapper.readTree("[{\"op\": \"remove\",\"path\": \"/b/0\"}]");
    }

    @Test
    public void withFlagAddShouldTreatMissingValuesAsNulls() throws IOException {
        JsonNode expected = mapper.readTree("{\"a\":null}");
        JsonNode result = JsonPatch.apply(new JsonPatch.JsonPatchParams(addNodeWithMissingValue, mapper.createObjectNode()).flags(EnumSet.of(MISSING_VALUES_AS_NULLS)));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagAddNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, EnumSet.of(MISSING_VALUES_AS_NULLS));
    }

    @Test
    public void withFlagReplaceShouldTreatMissingValuesAsNull() throws IOException {
        JsonNode source = mapper.readTree("{\"a\":\"test\"}");
        JsonNode expected = mapper.readTree("{\"a\":null}");
        JsonNode result = JsonPatch.apply(new JsonPatch.JsonPatchParams(replaceNodeWithMissingValue, source).flags(EnumSet.of(MISSING_VALUES_AS_NULLS)));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagReplaceNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, EnumSet.of(MISSING_VALUES_AS_NULLS));
    }

    @Test
    public void withFlagIgnoreRemoveNoneExistingArrayElement() throws IOException {
        JsonNode source = mapper.readTree("{\"b\": []}");
        JsonNode expected = mapper.readTree("{\"b\": []}");
        JsonNode result = JsonPatch.apply(new JsonPatch.JsonPatchParams(removeNoneExistingArrayElement, source).flags(EnumSet.of(REMOVE_NONE_EXISTING_ARRAY_ELEMENT)));
        assertThat(result, equalTo(expected));
    }
}
