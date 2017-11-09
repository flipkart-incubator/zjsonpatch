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

import java.util.regex.Pattern;

import com.google.common.base.Function;

final class EncodePathFunction implements Function<Object, String> {
    private static final EncodePathFunction INSTANCE = new EncodePathFunction();
    private static final Pattern TILDA_PATTERN = Pattern.compile("~");
    private static final Pattern SLASH_PATTERN = Pattern.compile("/");

    private EncodePathFunction() {
    }

    static EncodePathFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public String apply(Object object) {
        String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
        path = TILDA_PATTERN.matcher(path).replaceAll("~0");
        return SLASH_PATTERN.matcher(path).replaceAll("~1");
    }
}
