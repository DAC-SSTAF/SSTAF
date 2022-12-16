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

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static mil.sstaf.core.util.Validation.require;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {
    @Test
    void inlinePredicatesWork() {
        assertThrows(IllegalArgumentException.class, () -> require(-3, val -> val > 0, "Ack!"));
        assertThrows(IllegalArgumentException.class, () -> require(-3, val -> val > 0));
        assertEquals(3, require(3, val -> val > 0));
        assertEquals(3, require(3, val -> val > 0, "This is bad"));
    }

    @Test
    void declaredPredicateWorks() {
        Predicate<Object> predicate = o -> o instanceof String;

        assertThrows(IllegalArgumentException.class, () ->
                require(4, predicate));
        assertDoesNotThrow(() ->
                require("I am a string", predicate));

    }
}

