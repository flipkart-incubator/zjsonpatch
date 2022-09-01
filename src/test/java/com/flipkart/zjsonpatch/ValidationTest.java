package com.flipkart.zjsonpatch;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationTest {
    // the test cases have comments in them to explain the specific test case
    // hence use a custom Object mapper that allows Json comments
    private static final ObjectMapper MAPPER = new ObjectMapper()
                                                   .enable(JsonParser.Feature.ALLOW_COMMENTS);

    @ParameterizedTest
    @MethodSource("argsForValidationTest")
    public void testValidation(String patch) {
        assertThrows(
            InvalidJsonPatchException.class,
            () -> JsonPatch.validate(MAPPER.readTree(patch))
        );
    }

    public static Stream<Arguments> argsForValidationTest() throws IOException {
        JsonNode patches = MAPPER.readTree(TestUtils.loadFromResources("/testdata/invalid-patches.json"));

        List<Arguments> args = new ArrayList<>();

        for (JsonNode patch : patches) {
            args.add(Arguments.of(patch.toString()));
        }

        return args.stream();
    }
}
