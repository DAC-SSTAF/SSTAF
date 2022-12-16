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

package mil.devcom_sc.ansur.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GetValueMessageTest {

    static List<ValueKey> getMessages() {
        return Arrays.asList(ValueKey.values());
    }


    @Nested
    @DisplayName("Test the 'happy path'")
    class HappyTests {
        @ParameterizedTest(name = "{index} ==> Testing ''{0}''")
        @MethodSource("mil.devcom_sc.ansur.messages.GetValueMessageTest#getMessages")
        @DisplayName("Confirm that a GetValueMessage can be created, serialized and deserialized")
        void roundTrip(ValueKey valueKey) {
            ObjectMapper mapper = new ObjectMapper();
            GetValueMessage getValueMessage = GetValueMessage.builder().key(valueKey).build();
            assertNotNull(getValueMessage);
            assertEquals(valueKey, getValueMessage.key);

            assertDoesNotThrow(() -> {
                String json = mapper.writeValueAsString(getValueMessage);
                assertTrue(json.contains(valueKey.name()));
                JsonNode jsonNode = mapper.readTree(json);
                assertNotNull(jsonNode);
                GetValueMessage msg = mapper.treeToValue(jsonNode, GetValueMessage.class);
                assertNotNull(msg);
                assertNotNull(msg.key);
                assertEquals(valueKey, msg.key);
            });

        }
    }


    @Nested
    @DisplayName("Test failure modes")
    class FailureTests {
        @Test
        @DisplayName("Confirm that a null ValueKey throws")
        void test1() {
            NullPointerException npe = assertThrows(NullPointerException.class,
                    () -> {
                        GetValueMessage.builder().key(null).build();
                    });
            assertTrue(npe.getMessage().contains("key"));
        }

    }
}

