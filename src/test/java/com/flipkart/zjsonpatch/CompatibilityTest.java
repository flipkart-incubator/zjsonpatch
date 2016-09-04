package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.flipkart.zjsonpatch.CompatibilityFlags.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CompatibilityTest {

    ObjectMapper mapper;
    JsonNode addNodeWithMissingValue;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        addNodeWithMissingValue = mapper.readTree("[{\"op\":\"add\",\"path\":\"test\"}]");
    }

    @Test
    public void withFlagMissingValueShouldBeTreatedAsNulls() throws IOException {
        JsonNode expected = mapper.readTree("{\"a\":null}");
        JsonNode result = JsonPatch.apply(addNodeWithMissingValue, mapper.createObjectNode(), MISSING_VALUES_AS_NULLS);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagAddNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, MISSING_VALUES_AS_NULLS);
    }
}
