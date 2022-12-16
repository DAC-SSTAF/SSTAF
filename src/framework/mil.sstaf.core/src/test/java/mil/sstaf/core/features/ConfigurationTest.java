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

package mil.sstaf.core.features;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

    @Nested
    @DisplayName("Test the happy path")
    class HappyTests {

        @Test
        @DisplayName("Confirm that an empty configuration works and results in seed == 0")
        public void test1() {

            String jsonString = "{ \"class\" : \"" + FeatureConfiguration.class.getName() + "\"}";

            ObjectMapper mapper = new ObjectMapper();
            assertDoesNotThrow(() -> {
                FeatureConfiguration configuration = mapper.readValue(jsonString, FeatureConfiguration.class);
                assertEquals(0, configuration.getSeed());
            });
        }

        @Test
        @DisplayName("Confirm that setting the seed in the JSON works")
        public void test2() {

            String jsonString = "{ \"class\" : \"" + FeatureConfiguration.class.getName() + "\" , \"seed\" : 4569870123 }";

            ObjectMapper mapper = new ObjectMapper();
            assertDoesNotThrow(() -> {
                FeatureConfiguration configuration = mapper.readValue(jsonString, FeatureConfiguration.class);
                assertEquals(4569870123L, configuration.getSeed());
            });
        }


    }

}

