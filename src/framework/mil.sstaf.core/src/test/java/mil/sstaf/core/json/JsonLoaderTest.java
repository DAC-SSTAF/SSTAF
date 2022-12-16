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

import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonLoaderTest {


    public static final String PATH_STRING = "src/test/resources/jsonTests";
    public static final Path DEFAULT_PATH = Path.of("src/test/resources/jsonTests");

    private Path makePath(String file) {
        return Path.of(PATH_STRING, file).normalize();
    }

    @Nested
    @DisplayName("Test the 'Happy Path'")
    class HappyTests {
        @Test
        @DisplayName("Confirm that a simple JSON string can be processed")
        void loadTest1() {
            Assertions.assertDoesNotThrow(() -> {
                String x = "{ \"class\":\"mil.sstaf.core.json.Message1\", \"strings\":[]}";
                JsonLoader loader = new JsonLoader();
                Message1 msg1 = (Message1) loader.load(x, DEFAULT_PATH);
                Assertions.assertNotNull(msg1);
            });
        }

        @Test
        @DisplayName("Confirm that an object graph that contains references can be loaded")
        void loadTest2() {
            assertDoesNotThrow(() -> {
                JsonLoader jsonLoader = new JsonLoader();
                Message6 message6 = (Message6) jsonLoader.load(makePath("message6.json"));
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

        @Test
        @DisplayName("Confirm that absolute paths work during construction")
        void loadTest3() {
            assertDoesNotThrow(() -> {
                Path fullPath = Path.of(System.getProperty("user.dir"), PATH_STRING, "message6.json").toAbsolutePath();
                JsonLoader jsonLoader = new JsonLoader();
                Message6 message6 = (Message6) jsonLoader.load(fullPath);
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

        @Test
        @DisplayName("Confirm that absolute paths work when loading")
        void loadTest4() {
            assertDoesNotThrow(() -> {
                Path fullPath = Path.of(System.getProperty("user.dir"), PATH_STRING, "message6.json");
                JsonLoader jsonLoader = new JsonLoader();
                Message6 message6 = (Message6) jsonLoader.load(fullPath);
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
    @DisplayName("Test failure modes")
    class FailureTests {
        @Test
        @DisplayName("Confirm that circular references throw an exception.")
        void resolveTest5() {
            JsonResolutionException jre = assertThrows(JsonResolutionException.class, () -> {
                JsonLoader loader = new JsonLoader();
                Path topFile = makePath("message8.json");
                loader.load(topFile);
            });
            assertTrue(jre.getMessage().contains("Circular"));
        }

        @Test
        @DisplayName("Confirm that bad references throw an exception.")
        void resolveTest6() {
            JsonResolutionException jre = assertThrows(JsonResolutionException.class, () -> {
                JsonLoader loader = new JsonLoader();
                Path topFile = makePath("badref.json");
                loader.load(topFile);
            });
            assertTrue(jre.getMessage().contains("Could not load reference"));
            assertTrue(jre.getMessage().contains("not_there.json"));
        }

        @Test
        @DisplayName("Confirm that unknown classes throw an exception.")
        void resolveTest7() {
            SSTAFException se = assertThrows(SSTAFException.class, () -> {
                JsonLoader loader = new JsonLoader();
                Path topFile =makePath("badclass.json");
                loader.load(topFile);
            });
            assertTrue(se.getMessage().contains("Could not load"));
            assertTrue(se.getCause() instanceof ClassNotFoundException);
        }

        @Test
        @DisplayName("Confirm that a JSON object without a class field throws.")
        void resolveTest8() {
            SSTAFException se = assertThrows(SSTAFException.class, () -> {
                JsonLoader loader = new JsonLoader();
                Path topFile = makePath("noClassField.json");
                loader.load(topFile);
            });
            assertTrue(se.getMessage().contains("JSON does not specify a class name"));
        }

    }
}

