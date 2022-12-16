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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the MessageReader class
 */
public class MessageReaderTest {

    private MessageReader makeReader(String json) {
        StringReader sr = new StringReader(json);
        BufferedReader br = new BufferedReader(sr);
        return new MessageReader(br);
    }

    @Nested
    @DisplayName("Test scanAndCleanLine scenarios")
    class ScanTests {

        @Test
        @DisplayName("Check single open")
        void testSingle() {
            assertDoesNotThrow(() -> {
                String json = "{";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.scanAndCleanLine(json);
                assertEquals(1, messageReader.getOpenCurlies());
                assertEquals(0, messageReader.getOpenSquares());
                assertEquals(json, valid);
            });
        }

        @Test
        @DisplayName("Check open and closed")
        void testOpenAndClosed() {
            assertDoesNotThrow(() -> {
                String json = "{[]}";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.scanAndCleanLine(json);
                assertEquals(0, messageReader.getOpenCurlies());
                assertEquals(0, messageReader.getOpenSquares());
                assertEquals(json, valid);
            });
        }

        @Test
        @DisplayName("A multi-line Linux string gets flattened")
        void testFlattening1() {
            assertDoesNotThrow(() -> {
                String json = "{\n[\n]\n}";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.scanAndCleanLine(json);
                assertEquals(0, messageReader.getOpenCurlies());
                assertEquals(0, messageReader.getOpenSquares());
                assertEquals("{ [ ] }", valid);
            });
        }

        @Test
        @DisplayName("A multi-line MacOS string gets flattened")
        void testFlattening2() {
            assertDoesNotThrow(() -> {
                String json = "{\r[\r]\r}";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.scanAndCleanLine(json);
                assertEquals(0, messageReader.getOpenCurlies());
                assertEquals(0, messageReader.getOpenSquares());
                assertEquals("{ [ ] }", valid);
            });
        }

        @Test
        @DisplayName("A multi-line Windows string gets flattened")
        void testFlattening3() {
            assertDoesNotThrow(() -> {
                String json = "{\r\n[\r\n]\r\n}";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.scanAndCleanLine(json);
                assertEquals(0, messageReader.getOpenCurlies());
                assertEquals(0, messageReader.getOpenSquares());
                assertEquals("{ [ ] }", valid);
            });
        }

        @Test
        @DisplayName("Extra '}' throws")
        void extraCloseCurlyThrows() {
            SSTAFException sstafException =
                    assertThrows(SSTAFException.class, () -> {
                        String json = "{\r\n[\r\n]\r\n}}";
                        MessageReader messageReader = makeReader(json);
                        assertNotNull(messageReader);
                        messageReader.scanAndCleanLine(json);
                    });
            assertTrue(sstafException.getMessage().contains("Encountered extra '}'"));
        }

        @Test
        @DisplayName("Extra ']' throws")
        void extraCloseSquareThrows() {
            SSTAFException sstafException =
                    assertThrows(SSTAFException.class, () -> {
                        String json = "{\r\n[\r]\n]\r\n}}";
                        MessageReader messageReader = makeReader(json);
                        assertNotNull(messageReader);
                        messageReader.scanAndCleanLine(json);
                    });
            assertTrue(sstafException.getMessage().contains("Encountered extra '}'"));
        }
    }

    @Nested
    @DisplayName("Process well-formed messages (The Happy Path)")
    class HappyTests {
        @Test
        @DisplayName("Test empty on 1 line")
        void singleLineTest() {
            assertDoesNotThrow(() -> {
                String json = "{}";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.get();
                assertEquals(json, valid);
            });
        }

        @Test
        @DisplayName("Test complicated on multiple lines")
        void testComplicated() {
            assertDoesNotThrow(() -> {
                String json = "{ \"x\" : \"y\",\n \"p\": \n [ \"a\" , \"b\" ] }";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                String valid = messageReader.get();
                ObjectMapper om = new ObjectMapper();
                JsonNode tree = om.readTree(valid);
                assertTrue(tree.isObject());
                assertTrue(tree.has("x"));
                assertTrue(tree.has("p"));
            });
        }

        @Test
        @DisplayName("A sequence of complicated messages on multiple lines can be read")
        void testVeryComplicated() {
            assertDoesNotThrow(() -> {
                String json = "{ \"x\" : 0,\n \"p\": \n [ \"a\" , \"b\" ] }\n"
                        + "{ \"x\" : 1,\n \"p\": \n [ \"a\" , \"b\" ] }\n"
                        + "{ \"x\" : 2,\n \"p\": \n [ \"a\" , \"b\" ] }\n"
                        + "{ \"x\" : 3,\n \"p\": \n [ \"a\" , \"b\" ] }\n"
                        + "{ \"x\" : 4,\n \"p\": \n [ \"a\" , \"b\" ] }\n";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                for (int i = 0; i < 5; ++i) {
                    String valid = messageReader.get();
                    ObjectMapper om = new ObjectMapper();
                    JsonNode tree = om.readTree(valid);
                    assertTrue(tree.isObject());
                    ObjectNode obj = (ObjectNode) tree;
                    assertTrue(obj.has("x"));
                    assertEquals(i, obj.get("x").asInt());
                    assertTrue(tree.has("p"));
                }
            });
        }
    }

    @Nested
    @DisplayName("Test failure modes (The Unhappy Paths)")
    class UnhappyTests {
        @Test
        @DisplayName("Extra '}' throws")
        void testExtraCloseCurly() {
            SSTAFException sstafException = assertThrows(SSTAFException.class, () -> {
                String json = "}";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                messageReader.get();
            });
            assertTrue(sstafException.getMessage().contains("'}'"));
        }

        @Test
        @DisplayName("Test bad complicated on multiple lines")
        void testComplicated() {
            SSTAFException sstafException = assertThrows(SSTAFException.class, () -> {
                String json = "{ \"x\" : \"y\",\n \"p\": \n [ [ \"a\" , \"b\" ] }";
                MessageReader messageReader = makeReader(json);
                assertNotNull(messageReader);
                messageReader.get();
            });
            assertTrue(sstafException.getMessage().contains("Malformed JSON"));
        }
    }
}

