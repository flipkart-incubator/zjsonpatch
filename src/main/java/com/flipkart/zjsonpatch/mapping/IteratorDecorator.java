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

import java.util.Iterator;

/**
 * Decorator for Iterator to provide JsonNodeWrapper compatibility.
 * Wraps an Iterator of any type and provides Iterator&lt;JsonNodeWrapper&gt; interface.
 *
 * @param <E> the type of elements returned by the underlying iterator
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class IteratorDecorator<E> implements Iterator<JsonNodeWrapper> {

    private final Iterator<E> iterator;

    public IteratorDecorator(final Iterator<E> iterator) {
        if (iterator == null) {
            throw new NullPointerException();
        }
        this.iterator = iterator;
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
    public void remove() {
        iterator.remove();
    }

}
