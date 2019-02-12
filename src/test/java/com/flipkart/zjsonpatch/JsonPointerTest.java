package com.flipkart.zjsonpatch;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonPointerTest {

    // Parsing tests --

    @Test
    public void parsesRoot() {
        JsonPointer parsed = JsonPointer.parse("/");
        assertTrue(parsed.isRoot());
    }

    @Test
    public void parsesArrayIndirection() {
        JsonPointer parsed = JsonPointer.parse("/0");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(0, parsed.get(0).getIndex());
    }

    @Test
    public void parsesArrayTailIndirection() {
        JsonPointer parsed = JsonPointer.parse("/-");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(JsonPointer.LAST_INDEX, parsed.get(0).getIndex());
    }

    @Test
    public void parsesMultipleArrayIndirection() {
        JsonPointer parsed = JsonPointer.parse("/0/1");
        assertFalse(parsed.isRoot());
        assertEquals(2, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(0, parsed.get(0).getIndex());
        assertTrue(parsed.get(1).isArrayIndex());
        assertEquals(1, parsed.get(1).getIndex());
    }

    @Test
    public void parsesArrayIndirectionsWithLeadingZeroAsObjectIndirections() {
        JsonPointer parsed = JsonPointer.parse("/0123");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
    }

    @Test
    public void parsesArrayIndirectionsWithNegativeOffsetAsObjectIndirections() {
        JsonPointer parsed = JsonPointer.parse("/-3");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
    }

    @Test
    public void parsesObjectIndirections() {
        JsonPointer parsed = JsonPointer.parse("/a");
        assertFalse(parsed.isRoot());
        assertEquals(1, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("a", parsed.get(0).getField());
    }

    @Test
    public void parsesMultipleObjectIndirections() {
        JsonPointer parsed = JsonPointer.parse("/a/b");
        assertFalse(parsed.isRoot());
        assertEquals(2, parsed.size());
        assertFalse(parsed.get(0).isArrayIndex());
        assertEquals("a", parsed.get(0).getField());
        assertFalse(parsed.get(1).isArrayIndex());
        assertEquals("b", parsed.get(1).getField());
    }

    @Test
    public void parsesMixedIndirections() {
        JsonPointer parsed = JsonPointer.parse("/0/a/1/b");
        assertFalse(parsed.isRoot());
        assertEquals(4, parsed.size());
        assertTrue(parsed.get(0).isArrayIndex());
        assertEquals(0, parsed.get(0).getIndex());
        assertFalse(parsed.get(1).isArrayIndex());
        assertEquals("a", parsed.get(1).getField());
        assertTrue(parsed.get(2).isArrayIndex());
        assertEquals(1, parsed.get(2).getIndex());
        assertFalse(parsed.get(3).isArrayIndex());
        assertEquals("b", parsed.get(3).getField());
    }
}

