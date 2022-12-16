/*
 * Copyright (c) 2022
 * United States Government as represented by the U.S. Army DEVCOM Analysis Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mil.sstaf.core.util;

import java.util.function.Predicate;

/**
 * Simple utility methods for validating values.
 */
public class Validation {
    /**
     * Tests the provided target value using the provided {@code Predicate}
     *
     * @param target    the value to test
     * @param predicate the {@code Predicate} to use to test the value
     * @param message   the message to include in the {@code} print if the test fails.
     * @param <T>       the type of the value to test.
     * @return the target value
     */
    public static <T> T require(T target, Predicate<T> predicate, String message) {
        if (!predicate.test(target)) throw new IllegalArgumentException(message);
        return target;
    }

    /**
     * Tests the provided target value using the provided {@code Predicate}
     *
     * @param target    the value to test
     * @param predicate the {@code Predicate} to use to test the value
     * @param <T>       the type of the value to test.
     * @return the target value
     */
    public static <T> T require(T target, Predicate<T> predicate) {
        return require(target, predicate, target + " is not valid");
    }
}


