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

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jackson 3.x compatibility tests for JsonPointer functionality.
 * Tests the same functionality as {@link JsonPointerTest} but using Jackson 3.x APIs.
 *
 * @author Mariusz Sondecki
 */
class Jackson3JsonPointerTest {

    // Parsing tests --

    @Test
    void parsesEmptyStringAsRoot() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("");
        assertTrue(parsed.isRoot());
    }

    @Test
    void parsesSingleSlashAsFieldReference() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("", parsed.get(0).getField());
    }

    @Test
    void parsesArrayIndexIndirection() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/0");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(0, parsed.get(0).getIndex());
    }

    @Test
    void parsesArrayKeyRefIndirection() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/id=ID_1");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertTrue(parsed.get(0).isArrayKeyRef());
        assertEquals(new KeyRef("id", "ID_1"), parsed.get(0).getKeyRef());
    }

    @Test
    void parsesArrayTailIndirection() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/-");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(Jackson3JsonPointer.LAST_INDEX, parsed.get(0).getIndex());
    }

    @Test
    void parsesMultipleArrayIndirection() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/0/1");
        assertFalse(parsed.isRoot());
        assertEquals(2, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(0, parsed.get(0).getIndex());
        assertTrue(parsed.get(1).isArrayIndex());
        assertEquals(1, parsed.get(1).getIndex());
    }

    @Test
    void parsesArrayIndirectionsWithLeadingZeroAsObjectIndirections() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/0123");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
    }

    @Test
    void parsesArrayIndirectionsWithNegativeOffsetAsObjectIndirections() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/-3");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
    }

    @Test
    void parsesObjectIndirections() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/a");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("a", parsed.get(0).getField());
    }

    @Test
    void parsesMultipleObjectIndirections() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/a/b");
        assertFalse(parsed.isRoot());
        assertEquals(2, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("a", parsed.get(0).getField());
        assertFalse(parsed.get(1).isArrayIndex());
        assertEquals("b", parsed.get(1).getField());
    }

    @Test
    void parsesMixedIndirections() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/0/a/1/b/id=2/c");
        assertFalse(parsed.isRoot());
        assertEquals(6, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(0, parsed.get(0).getIndex());
        assertFalse(parsed.get(1).isArrayIndex());
        assertEquals("a", parsed.get(1).getField());
        assertTrue(parsed.get(2).isArrayIndex());
        assertEquals(1, parsed.get(2).getIndex());
        assertFalse(parsed.get(3).isArrayIndex());
        assertEquals("b", parsed.get(3).getField());
        assertFalse(parsed.get(4).isArrayIndex());
        assertTrue(parsed.get(4).isArrayKeyRef());
        assertEquals(new KeyRef("id", "2"), parsed.get(4).getKeyRef());
        assertFalse(parsed.get(5).isArrayIndex());
        assertFalse(parsed.get(5).isArrayKeyRef());
        assertEquals("c", parsed.get(5).getField());
    }

    @Test
    void parsesEscapedTilde() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/~0");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("~", parsed.get(0).getField());
    }

    @Test
    void parsesEscapedForwardSlash() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/~1");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("/", parsed.get(0).getField());
    }

    @Test
    void parsesEscapedEqualsSign() {
        Jackson3JsonPointer parsed = Jackson3JsonPointer.parse("/~2");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("=", parsed.get(0).getField());
    }

    // Parsing error conditions --

    @Test
    void throwsOnMissingLeadingSlash() {
        assertThrows(IllegalArgumentException.class, () -> {
            Jackson3JsonPointer.parse("illegal/reftoken");
        });
    }

    @Test
    void throwsOnInvalidEscapedSequence1() {
        assertThrows(IllegalArgumentException.class, () -> {
            Jackson3JsonPointer.parse("/~3");
        });
    }

    @Test
    void throwsOnInvalidEscapedSequence2() {
        assertThrows(IllegalArgumentException.class, () -> {
            Jackson3JsonPointer.parse("/~a");
        });
    }

    // Evaluation tests --

    @Test
    void evaluatesAccordingToRFC6901() throws IOException, JsonPointerEvaluationException {
        // Tests resolution according to https://tools.ietf.org/html/rfc6901#section-5

        ObjectMapper om = Jackson3TestUtils.DEFAULT_MAPPER;
        JsonNode data = Jackson3TestUtils.loadResourceAsJsonNode("/rfc6901/data.json");
        JsonNode testData = data.get("testData");

        assertEquals(om.readTree("[\"bar\", \"baz\"]"), Jackson3JsonPointer.parse("/foo").evaluate(testData));
        assertEquals(om.readTree("\"bar\""), Jackson3JsonPointer.parse("/foo/0").evaluate(testData));
        assertEquals(om.readTree("0"), Jackson3JsonPointer.parse("/").evaluate(testData));
        assertEquals(om.readTree("1"), Jackson3JsonPointer.parse("/a~1b").evaluate(testData));
        assertEquals(om.readTree("2"), Jackson3JsonPointer.parse("/c%d").evaluate(testData));
        assertEquals(om.readTree("3"), Jackson3JsonPointer.parse("/e^f").evaluate(testData));
        assertEquals(om.readTree("4"), Jackson3JsonPointer.parse("/g|h").evaluate(testData));
        assertEquals(om.readTree("5"), Jackson3JsonPointer.parse("/i\\j").evaluate(testData));
        assertEquals(om.readTree("6"), Jackson3JsonPointer.parse("/k\"l").evaluate(testData));
        assertEquals(om.readTree("7"), Jackson3JsonPointer.parse("/ ").evaluate(testData));
        assertEquals(om.readTree("8"), Jackson3JsonPointer.parse("/m~0n").evaluate(testData));
    }

    @Test
    void evaluatesWithKeyReferences() throws IOException, JsonPointerEvaluationException {
        ObjectMapper om = Jackson3TestUtils.DEFAULT_MAPPER;
        JsonNode data = Jackson3TestUtils.loadResourceAsJsonNode("/testdata/json-pointer-key-refs.json");
        JsonNode testData = data.get("testData");

        assertEquals(om.readTree("{\"id\": \"ID_1\",\"some\": \"data_1\"}"), Jackson3JsonPointer.parse("/objArray/id=ID_1").evaluate(testData));
        assertEquals(om.readTree("\"data_1\""), Jackson3JsonPointer.parse("/objArray/id=ID_1/some").evaluate(testData));
        assertEquals(om.readTree("\"data_2\""), Jackson3JsonPointer.parse("/objArray/id=ID_2/some").evaluate(testData));
        assertEquals(om.readTree("\"some_more\""), Jackson3JsonPointer.parse("/objArray/id=ID_2/and").evaluate(testData));
        assertEquals(om.readTree("7"), Jackson3JsonPointer.parse("/objArray/id=ID_2/num").evaluate(testData));
        assertEquals(om.readTree("\"data_2\""), Jackson3JsonPointer.parse("/objArray/and=some_more/some").evaluate(testData));
        assertEquals(om.readTree("\"data_3\""), Jackson3JsonPointer.parse("/objArray/id=ID_3/some").evaluate(testData));
        assertEquals(om.readTree("\"data_4\""), Jackson3JsonPointer.parse("/objArray/id=ID_4/some").evaluate(testData));
        assertEquals(om.readTree("\"data_4\""), Jackson3JsonPointer.parse("/objArray/3/some").evaluate(testData));

        assertThrows(JsonPointerEvaluationException.class, () -> Jackson3JsonPointer.parse("/objArray/id=ID_5").evaluate(testData));
        assertThrows(JsonPointerEvaluationException.class, () -> Jackson3JsonPointer.parse("/objArray/ref=REF").evaluate(testData));
        assertThrows(JsonPointerEvaluationException.class, () -> Jackson3JsonPointer.parse("/objArray/4").evaluate(testData));
    }

    // Utility methods --

    @Test
    void rendersRootToEmptyString() {
        assertEquals("", Jackson3JsonPointer.ROOT.toString());
    }

    @Test
    void symmetricalInParsingAndRendering() {
        assertEquals("/foo", Jackson3JsonPointer.parse("/foo").toString());
        assertEquals("/foo/0", Jackson3JsonPointer.parse("/foo/0").toString());
        assertEquals("/", Jackson3JsonPointer.parse("/").toString());
        assertEquals("/a~1b", Jackson3JsonPointer.parse("/a~1b").toString());
        assertEquals("/c%d", Jackson3JsonPointer.parse("/c%d").toString());
        assertEquals("/e^f", Jackson3JsonPointer.parse("/e^f").toString());
        assertEquals("/g|h", Jackson3JsonPointer.parse("/g|h").toString());
        assertEquals("/i\\j", Jackson3JsonPointer.parse("/i\\j").toString());
        assertEquals("/k\"l", Jackson3JsonPointer.parse("/k\"l").toString());
        assertEquals("/ ", Jackson3JsonPointer.parse("/ ").toString());
        assertEquals("/m~0n", Jackson3JsonPointer.parse("/m~0n").toString());
        assertEquals("/m=n", Jackson3JsonPointer.parse("/m=n").toString());
        assertEquals("/m~2n", Jackson3JsonPointer.parse("/m~2n").toString());
    }
}
