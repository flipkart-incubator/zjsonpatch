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

package com.flipkart.zjsonpatch.mapping;

import java.util.ListIterator;
import java.util.Objects;

/**
 * Decorator that wraps a ListIterator to provide JsonNodeWrapper instances
 * instead of raw JSON nodes, enabling version-agnostic iteration.
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class ListIteratorDecorator<E> implements ListIterator<JsonNodeWrapper> {

    private final ListIterator<E> iterator;

    public ListIteratorDecorator(final ListIterator<E> iterator) {
        this.iterator = Objects.requireNonNull(iterator, "iterator");
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public JsonNodeWrapper next() {
        final E next = iterator.next();
        return JacksonVersionBridge.wrap(next);
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public JsonNodeWrapper previous() {
        final E previous = iterator.previous();
        return JacksonVersionBridge.wrap(previous);
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void set(final JsonNodeWrapper obj) {
        iterator.set(JacksonVersionBridge.unwrap(obj));
    }

    @Override
    public void add(final JsonNodeWrapper obj) {
        iterator.add(JacksonVersionBridge.unwrap(obj));
    }

}
