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

/**
 * Exception thrown when Jackson version-related operations fail.
 * <p>
 * This exception is thrown in scenarios such as:
 * <ul>
 *   <li>The required Jackson version is not available on the classpath</li>
 *   <li>Unable to load version-specific wrapper or factory classes</li>
 *   <li>Reflection-based instantiation of Jackson components fails</li>
 *   <li>Unsupported Jackson node types are encountered</li>
 * </ul>
 * <p>
 * This exception helps distinguish between Jackson version issues and
 * other runtime problems, making it easier to diagnose configuration
 * or classpath problems.
 *
 * @author Mariusz Sondecki
 * @since 0.6.0
 */
public class JacksonVersionException extends RuntimeException {

    public JacksonVersionException(String message) {
        super(message);
    }

    public JacksonVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JacksonVersionException(Throwable cause) {
        super(cause);
    }
}
