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

package mil.sstaf.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSimpleThings {
    private static final char QUOTE = '"';
    private static final char COLON = ':';
    private static final char COMMA = ',';
    private static final char START_OBJ = '{';
    private static final char END_OBJ = '}';
    private static final char START_LIST = '[';
    private static final char END_LIST = ']';
    private static final String CN = "class";
    private static final int NUM_STRINGS = 4;
    private static final int NUM_INTS = 7;
    private static final int NUM_MSGS = 12;

    final Random random = new Random(987654321L);

    String makeClassName(final Class<?> c) {
        return QUOTE + CN + QUOTE + COLON + QUOTE + c.getName() + QUOTE;
    }

    String wrapObj(String contents) {
        return START_OBJ + contents + END_OBJ;
    }

    String makeMessage1Contents() {
        String[] names = new String[]{
                "Thorin", "Balin", "Dwalin", "Kili", "Fili", "Dori", "Nori", "Ori",
                "Bifur", "Bofur", "Bombur", "Oin", "Gloin", "Bilbo", "Ganfalf"};
        StringBuilder sb = new StringBuilder();
        sb.append(QUOTE).append("strings").append(QUOTE).append(COLON);
        sb.append(START_LIST);
        for (int i = 0; i < NUM_STRINGS ; ++i) {
            String s = names[random.nextInt(names.length)];
            sb.append(QUOTE).append(s).append(QUOTE);
            if (i < (NUM_STRINGS - 1 )) sb.append(COMMA);
        }

        sb.append(END_LIST);
        return sb.toString();
    }

    String makeJSONForMessage1() {
        String sb = makeClassName(Message1.class) + COMMA +
                makeMessage1Contents();
       return wrapObj(sb);
    }

    String makeJSONForMessage2() {
        StringBuilder sb = new StringBuilder();
        sb.append(makeClassName(Message2.class)).append(COMMA);
        sb.append(makeMessage1Contents()).append(COMMA);
        sb.append(QUOTE).append("ints").append(QUOTE).append(COLON);
        sb.append(START_LIST);
        for (int i = 0; i < NUM_INTS; ++i) {
            sb.append(random.nextInt());
            if (i < NUM_INTS - 1) sb.append(COMMA);
        }
        sb.append(END_LIST);
        return wrapObj(sb.toString());
    }

    String makeJSONForMessage3() {
        StringBuilder sb = new StringBuilder();
        sb.append(makeClassName(Message3.class)).append(COMMA);
        sb.append(QUOTE).append("msgs").append(QUOTE).append(COLON);
        sb.append(START_LIST);
        for (int i = 0; i < NUM_MSGS; ++i) {
            sb.append( i %2 == 0 ? makeJSONForMessage2() : makeJSONForMessage1());
            if (i < NUM_MSGS - 1) sb.append(COMMA);
        }
        sb.append(END_LIST);
        return wrapObj(sb.toString());
    }

    @Nested
    @DisplayName("Test scenarios where the input is valid (The happy path)")
    class HappyTests {

        @Test
        @DisplayName("Confirm that deserializing Message1 works")
        public void test1() throws JsonProcessingException {
            String jsonString = makeJSONForMessage1();
            ObjectMapper objectMapper = new ObjectMapper();
            Message1 message1 = objectMapper.readValue(jsonString, Message1.class);
            assertNotNull(message1);
            assertEquals(NUM_STRINGS, message1.getStrings().size());
        }

        @Test
        @DisplayName("Confirm that deserializing Message2 works")
        public void test2() throws JsonProcessingException {
            String jsonString = makeJSONForMessage2();
            ObjectMapper objectMapper = new ObjectMapper();
            Message2 message2 = objectMapper.readValue(jsonString, Message2.class);
            assertNotNull(message2);
            assertEquals(NUM_STRINGS, message2.getStrings().size());
            assertEquals(NUM_INTS, message2.getInts().size());
        }

        private String getCurrentStackFrame() {
            Thread t = Thread.currentThread();
            StackTraceElement[] stack = t.getStackTrace();
            StackTraceElement caller = stack[2];
            return caller.getMethodName();
        }

        @Test
        @DisplayName("Confirm that deserializing a known subclass works")
        public void test3() throws JsonProcessingException {
            String jsonString =
                    "{ \"strings\":[\"Alan\", \"Ellie\", \"Ian\"]," +
                            "\"integerList\":[1,2,3,4,5,6]}";
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonString);
            assertNotNull(node);
            assertNotNull(getCurrentStackFrame());
        }

        @Test
        @DisplayName("Check how mixed collections work")
        public void test4() throws JsonProcessingException {
            String jsonString = makeJSONForMessage3();
            ObjectMapper objectMapper = new ObjectMapper();
            Message3 msg3 = objectMapper.readValue(jsonString, Message3.class);
            Assertions.assertNotNull(msg3);
            assertEquals(NUM_MSGS, msg3.getMsgs().size());
            int i = 0;
            for (Message1 m : msg3.getMsgs()) {
               if (i % 2 == 0) {
                   Assertions.assertTrue(m instanceof Message2);
               } else {
                   Assertions.assertFalse(m instanceof Message2);
               }
               ++i;
            }
        }

        @Test
        @DisplayName("Play with MyObjectMapper")
        public void test5() throws JsonProcessingException {
            String jsonString = makeJSONForMessage3();
            ObjectMapper objectMapper = new ObjectMapper();
            Message3 msg3 = objectMapper.readValue(jsonString, Message3.class);
            var tf = objectMapper.getTypeFactory();
            tf.constructFromCanonical(Message2.class.getCanonicalName());
            tf.constructFromCanonical(Message1.class.getCanonicalName());
            Assertions.assertNotNull(msg3);
            assertEquals(NUM_MSGS, msg3.getMsgs().size());
            boolean foundMsg2 = false;
            for (Message1 m : msg3.getMsgs()) {
                if (m instanceof Message2) foundMsg2 = true;
            }
            Assertions.assertTrue(foundMsg2);
        }

        @Test
        @DisplayName("Test Lombok builder behavior")
        public void test100() {
            Message2.builder()
                    .ints(List.of(1, 2, 3, 4, 5, 6))
                    .strings(List.of("Alan", "Ellie", "Ian"));

        }

    }

    class FailureTests {

    }
}

