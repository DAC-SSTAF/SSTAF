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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class WrapperTest {

    @Nested
    @DisplayName("Test the 'Happy Paths'")
    class HappyTests {
        @Test
        @DisplayName("Confirm that reference counting works")
        public void testRefCount1() {
            ObjectMapperFactory omf = path -> null;
            Map<String, JsonNode> cache = new HashMap<>();

            ReferenceWrapper one = new ReferenceWrapper("bob", null, null, omf, cache);
            ReferenceWrapper two = new ReferenceWrapper("bob", one, null, omf, cache);
            ReferenceWrapper three = new ReferenceWrapper("diane", two, null, omf, cache);
            ReferenceWrapper four = new ReferenceWrapper("bob", three, null, omf, cache);

            assertEquals(3, four.refCount("bob"));
            assertEquals(1, four.refCount("diane"));
            assertEquals(0, four.refCount("luigi"));
        }

        @Test
        @DisplayName("Simplest test to confirm that a ReferenceWrapper can resolve a reference")
        void resolveTest1() {
            assertDoesNotThrow(() -> {
                Map<String, JsonNode> referenceCache = new HashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                //
                // Create the JsonNode that will be inserted into the graph.
                // Stick it in the cache.
                //
                String jsonString1 = "{\"class\":\"mil.sstaf.core.json.Message2\", \"ints\":[1,2,3,4,5,6]}";
                JsonNode node1 = omf.create(null).readTree(jsonString1);
                String referenceName = "/path/to/file.json";
                referenceCache.put(referenceName, node1);

                //
                // Create the top-level object that uses a reference
                //
                String jsonString2 = "{\"it\":\"/path/to/file.json\"}";
                JsonNode topNode = omf.create(null).readTree(jsonString2);
                JsonNode itNode = topNode.get("it");
                assertEquals(referenceName, itNode.asText());

                //
                // Build the wrappers
                //
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);
                ReferenceWrapper wrapper = new ReferenceWrapper(
                        itNode.asText(), topWrapper, itNode, omf, referenceCache);

                //
                // Resolve the reference and confirm
                //
                wrapper.resolve();
                assertTrue(wrapper.resolved);
                JsonNode replaced = topNode.get("it");
                assertEquals(node1, replaced);
            });
        }

        @Test
        @DisplayName("Try invoking resolution from the top level")
        void resolveTest2() {
            assertDoesNotThrow(() -> {
                Map<String, JsonNode> referenceCache = new HashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                //
                // Create the JsonNode that will be inserted into the graph.
                // Stick it in the cache.
                //
                String jsonString1 = "{\"class\":\"mil.sstaf.core.json.Message2\", \"ints\":[1,2,3,4,5,6]}";
                JsonNode node1 = omf.create(null).readTree(jsonString1);
                String referenceName = "/path/to/file.json";
                referenceCache.put(referenceName, node1);

                //
                // Create the top-level object that uses a reference
                //
                String jsonString2 = "{\"it\":\"/path/to/file.json\"}";
                JsonNode topNode = omf.create(null).readTree(jsonString2);
                JsonNode itNode = topNode.get("it");
                assertEquals(referenceName, itNode.asText());

                //
                // Build the top-level wrappers
                //
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);

                //
                // Resolve the reference and confirm
                //
                topWrapper.resolve();
                assertTrue(topWrapper.resolved);
                JsonNode replaced = topNode.get("it");
                assertEquals(node1, replaced);
            });
        }

        @Test
        @DisplayName("Confirm that deserialization works for two-node system.")
        void resolveTest3() {
            assertDoesNotThrow(() -> {
                Map<String, JsonNode> referenceCache = new HashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                //
                // Create the JsonNode that will be inserted into the graph.
                // Stick it in the cache.
                //
                String jsonString1 = "{\"class\":\"mil.sstaf.core.json.Message2\", \"ints\":[1,2,3,4,5,6]}";
                JsonNode node1 = omf.create(null).readTree(jsonString1);
                String referenceName = "/path/to/file.json";
                referenceCache.put(referenceName, node1);

                //
                // Create the top-level object that uses a reference
                //
                String jsonString2 = "{\"class\":\"mil.sstaf.core.json.Message4\"," +
                        " \"it\":\"/path/to/file.json\"}";
                JsonNode topNode = omf.create(null).readTree(jsonString2);
                JsonNode itNode = topNode.get("it");
                assertEquals(referenceName, itNode.asText());

                //
                // Build the top-level wrappers
                //
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);

                //
                // Resolve the reference and confirm
                //
                topWrapper.resolve();
                assertTrue(topWrapper.resolved);
                JsonNode replaced = topNode.get("it");
                assertEquals(node1, replaced);
                ObjectMapper bob = omf.create(null);
                Message4 message4 = bob.treeToValue(topNode, Message4.class);
                assertNotNull(message4);
                assertNotNull(message4.getIt());
                Message2 message2 = message4.getIt();
                assertEquals(6, message2.getInts().size());
            });
        }

        @Test
        @DisplayName("Confirm that deserialization works for file-based, three-node system.")
        void resolveTest4() {
            assertDoesNotThrow(() -> {
                Map<String, JsonNode> referenceCache = new ConcurrentHashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                String userDir = System.getProperty("user.dir");
                Path basePath = Path.of(userDir, "src/test/resources/jsonTests");

                Path topFile = Path.of(basePath.toString(), "message5.json");
                JsonNode topNode = omf.create(null).readTree(topFile.toFile());
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);
                topWrapper.setDirectory(basePath);

                //
                // Resolve the reference and confirm
                //
                topWrapper.resolve();
                assertTrue(topWrapper.resolved);
                ObjectMapper bob = omf.create(null);
                Message5 message5 = bob.treeToValue(topNode, Message5.class);
                assertNotNull(message5);
                assertNotNull(message5.getMsg4());
                Message2 message2 = message5.getMsg4().getIt();
                assertEquals(6, message2.getInts().size());
            });
        }

        @Test
        @DisplayName("Confirm that deserialization works for arrays of references.")
        void resolveTest5() {
            assertDoesNotThrow(() -> {
                Map<String, JsonNode> referenceCache = new ConcurrentHashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                String userDir = System.getProperty("user.dir");
                Path basePath = Path.of(userDir, "src/test/resources/jsonTests");

                Path topFile = Path.of(basePath.toString(), "message6.json");
                JsonNode topNode = omf.create(null).readTree(topFile.toFile());
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);
                topWrapper.setDirectory(basePath);

                //
                // Resolve the reference and confirm
                //
                topWrapper.resolve();
                assertEquals(2, referenceCache.size());
                assertTrue(topWrapper.resolved);
                ObjectMapper bob = omf.create(null);
                Message6 message6 = bob.treeToValue(topNode, Message6.class);
                assertNotNull(message6);
                assertNotNull(message6.getFours());
                List<Message4> fours = message6.getFours();
                assertEquals(4, fours.size());
                for (Message4 msg4 : fours) {
                    assertNotNull(msg4.getIt());
                    Message2 msg2 = msg4.getIt();
                    assertEquals(6, msg2.getInts().size());
                }
            });
        }
    }

    @Nested
    @DisplayName("Test scenarios that cause failures")
    class FailureTests {
        @Test
        @DisplayName("Confirm that circular references throw an exception.")
        void resolveTest5() {
            JsonResolutionException jre = assertThrows(JsonResolutionException.class, () -> {
                Map<String, JsonNode> referenceCache = new ConcurrentHashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                String userDir = System.getProperty("user.dir");
                Path basePath = Path.of(userDir, "src/test/resources/jsonTests");

                Path topFile = Path.of(basePath.toString(), "message8.json");
                JsonNode topNode = omf.create(null).readTree(topFile.toFile());
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);
                topWrapper.setDirectory(basePath);

                //
                // Resolve the reference and confirm
                //
                topWrapper.resolve();
            });
            assertTrue(jre.getMessage().contains("Circular"));
        }


        @Test
        @DisplayName("Confirm that bad references throw an exception.")
        void resolveTest6() {
            JsonResolutionException jre = assertThrows(JsonResolutionException.class, () -> {
                Map<String, JsonNode> referenceCache = new ConcurrentHashMap<>();
                ObjectMapperFactory omf = path -> new ObjectMapper();

                String userDir = System.getProperty("user.dir");
                Path basePath = Path.of(userDir, "src/test/resources/jsonTests");

                Path topFile = Path.of(basePath.toString(), "badref.json");
                JsonNode topNode = omf.create(null).readTree(topFile.toFile());
                ObjectNodeWrapper topWrapper = new ObjectNodeWrapper(null,
                        (ObjectNode) topNode, omf, referenceCache);
                topWrapper.setDirectory(basePath);

                //
                // Resolve the reference and confirm
                //
                topWrapper.resolve();
            });
            assertTrue(jre.getMessage().contains("Could not read reference"));
            assertTrue(jre.getMessage().contains("not_there.json"));
        }
    }
}
