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

package com.flipkart.zjsonpatch.mapping.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;
import com.flipkart.zjsonpatch.mapping.JacksonVersionBridge;
import com.flipkart.zjsonpatch.mapping.JsonNodeWrapper;
import com.flipkart.zjsonpatch.mapping.jackson3.Jackson3ArrayNodeWrapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Jackson2ArrayNodeWrapperTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {"[]", "[1]", "[1,2,3]", "[null,true,\"value\"]", "[[],{},[1]]"})
    void iteratesArrayNodes(String json) throws Exception {
        ArrayNode array = (ArrayNode) MAPPER.readTree(json);
        Iterator<JsonNodeWrapper> iterator = new Jackson2ArrayNodeWrapper(array).iterator();
        int index = 0;
        while (iterator.hasNext()) {
            assertSame(array.get(index++), JacksonVersionBridge.unwrap(iterator.next()));
        }
        assertEquals(array.size(), index);
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void removeChangesTheOriginalArray() throws Exception {
        ArrayNode array = (ArrayNode) MAPPER.readTree("[1,2,3]");
        Iterator<JsonNodeWrapper> iterator = new Jackson2ArrayNodeWrapper(array).iterator();
        iterator.next();
        iterator.remove();
        assertEquals(MAPPER.readTree("[2,3]"), array);
        assertSame(array.get(0), JacksonVersionBridge.unwrap(iterator.next()));
    }

    @Test
    void removeRequiresNext() throws Exception {
        Iterator<JsonNodeWrapper> iterator = new Jackson2ArrayNodeWrapper(
            (ArrayNode) MAPPER.readTree("[1]")).iterator();
        assertThrows(IllegalStateException.class, iterator::remove);
    }

    @Test
    void removeCannotBeRepeatedWithoutNext() throws Exception {
        ArrayNode array = (ArrayNode) MAPPER.readTree("[1,2]");
        Iterator<JsonNodeWrapper> iterator = new Jackson2ArrayNodeWrapper(array).iterator();
        iterator.next();
        iterator.remove();
        assertThrows(IllegalStateException.class, iterator::remove);
        assertEquals(MAPPER.readTree("[2]"), array);
    }

    @Test
    void forEachRemainingContinuesFromCurrentPosition() throws Exception {
        ArrayNode array = (ArrayNode) MAPPER.readTree("[1,2,3]");
        Iterator<JsonNodeWrapper> iterator = new Jackson2ArrayNodeWrapper(array).iterator();
        iterator.next();
        List<JsonNode> remaining = new ArrayList<>();
        iterator.forEachRemaining(node -> remaining.add(JacksonVersionBridge.unwrap(node)));
        assertEquals(List.of(array.get(1), array.get(2)), remaining);
        assertFalse(iterator.hasNext());
    }

    static Stream<Arguments> operations() {
        return Stream.of(
            Arguments.of("[{\"op\":\"add\",\"path\":\"/items/-\",\"value\":3}]", "{\"items\":[1,2,3],\"answer\":0,\"other\":[3]}"),
            Arguments.of("[{\"op\":\"remove\",\"path\":\"/items/0\"}]", "{\"items\":[2],\"answer\":0,\"other\":[3]}"),
            Arguments.of("[{\"op\":\"replace\",\"path\":\"/answer\",\"value\":1}]", "{\"items\":[1,2],\"answer\":1,\"other\":[3]}"),
            Arguments.of("[{\"op\":\"move\",\"from\":\"/other/0\",\"path\":\"/items/0\"}]", "{\"items\":[3,1,2],\"answer\":0,\"other\":[]}"),
            Arguments.of("[{\"op\":\"copy\",\"from\":\"/items/0\",\"path\":\"/answer\"}]", "{\"items\":[1,2],\"answer\":1,\"other\":[3]}"),
            Arguments.of("[{\"op\":\"test\",\"path\":\"/answer\",\"value\":0}]", "{\"items\":[1,2],\"answer\":0,\"other\":[3]}"));
    }

    private JsonNode source() throws Exception {
        return MAPPER.readTree("{\"items\":[1,2],\"answer\":0,\"other\":[3]}");
    }

    @ParameterizedTest
    @MethodSource("operations")
    void appliesPatchesWithoutChangingTheSource(String patch, String expected) throws Exception {
        JsonNode source = source();
        assertEquals(MAPPER.readTree(expected), JsonPatch.apply(MAPPER.readTree(patch), source));
        assertEquals(source(), source);
    }

    @ParameterizedTest
    @MethodSource("operations")
    void appliesPatchesInPlace(String patch, String expected) throws Exception {
        JsonNode source = source();
        JsonPatch.applyInPlace(MAPPER.readTree(patch), source);
        assertEquals(MAPPER.readTree(expected), source);
    }

    @ParameterizedTest
    @MethodSource("operations")
    void validatesPatches(String patch, String expected) throws Exception {
        JsonPatch.validate(MAPPER.readTree(patch));
    }

    @Test
    void jackson3IterationRemainsSupported() {
        tools.jackson.databind.node.ArrayNode array = tools.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        array.add(1);
        Iterator<JsonNodeWrapper> iterator = new Jackson3ArrayNodeWrapper(array).iterator();
        assertSame(array.get(0), JacksonVersionBridge.unwrap(iterator.next()));
        assertFalse(iterator.hasNext());
    }

    @Test
    void supportsPlainIteratorContract() {
        ArrayNode array = new ArrayNode(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance) {
            @Override
            public Iterator<JsonNode> elements() {
                // ArrayNode promises Iterator, not ListIterator.
                Iterator<JsonNode> delegate = super.elements();
                return new Iterator<JsonNode>() {
                    public boolean hasNext() { return delegate.hasNext(); }
                    public JsonNode next() { return delegate.next(); }
                    public void remove() { delegate.remove(); }
                };
            }
        };
        array.add(1).add(2);
        Iterator<JsonNodeWrapper> iterator = new Jackson2ArrayNodeWrapper(array).iterator();
        assertSame(array.get(0), JacksonVersionBridge.unwrap(iterator.next()));
        iterator.remove();
        assertEquals(1, array.size());
        assertSame(array.get(0), JacksonVersionBridge.unwrap(iterator.next()));
        assertFalse(iterator.hasNext());
    }
}
