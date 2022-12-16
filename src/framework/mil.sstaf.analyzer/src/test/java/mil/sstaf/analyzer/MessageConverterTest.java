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

package mil.sstaf.analyzer;

import mil.sstaf.analyzer.messages.BaseAnalyzerCommand;
import mil.sstaf.analyzer.messages.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageConverterTest {

    private JsonDeserializer messageConverter;

    @BeforeEach
    void setup() {
        messageConverter = new JsonDeserializer();
    }

    @Nested
    @DisplayName("Test normal behavior (The Happy Path)")
    class HappyTests {

        @Test
        @DisplayName("Confirm a Tick can be generated")
        void tickTest() {
            assertDoesNotThrow(() -> {
                String json = "{ \"class\" : \"mil.sstaf.analyzer.messages.Tick\","
                        + "\"time_ms\" : 1000 }";
                BaseAnalyzerCommand thing = messageConverter.apply(json);
                assertNotNull(thing);
                assertTrue(thing instanceof Tick);
            });
        }


    }
}

