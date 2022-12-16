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

package mil.sstaf.core.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HumanFactoryTest {

    static Path USER_DIR = Path.of(System.getProperty("user.dir"));
    static Path RESOURCE_DIR = Path.of(USER_DIR.toString(), "src/test/resources/HumanFactoryTest");

    private File makeFile(String filename) {
        Path path = Path.of(RESOURCE_DIR.toString(), filename);
        return path.toFile();
    }

    @Nested
    @DisplayName("Test the 'Happy Path'")
    class HappyTests {

        @Test
        @DisplayName("Confirm that a configuration file that consists only of references can be loaded correctly")
        void allReferenceJSONFileWorks() {
            Assertions.assertDoesNotThrow(() -> {
                String filename = "TestHuman1.json";
                File f = makeFile(filename);
                Human human = Human.from(f);
                assertNotNull(human);
            });
        }

        @Test
        @DisplayName("Confirm that a good JSON configuration file can be loaded")
        void goodJSONWorks() {
            ObjectMapper om = new ObjectMapper();
            ObjectNode jsonObject = om.createObjectNode();
            jsonObject.put("class", "mil.sstaf.core.entity.Human");
            jsonObject.put("name", "Test Dude");
            ObjectNode procNode = om.createObjectNode();
            jsonObject.set("configurations", procNode);
            jsonObject.set("features", om.createArrayNode());
            Human human = Human.from(jsonObject, RESOURCE_DIR);
            assertNotNull(human);
        }


    }

    @Nested
    @DisplayName("Test failure modes")
    class FailureTests {
        @Test
        @DisplayName("Confirm that a attempting to load a malformed file will throw a SSTAFException")
        void badJSONThrows() {
            assertThrows(SSTAFException.class, () -> {
                ObjectMapper om = new ObjectMapper();
                ObjectNode jsonObject = om.createObjectNode();
                Human human = Human.from(jsonObject, RESOURCE_DIR);
                assertNotNull(human);
            });
        }

        @Test
        void missingFileThrows() {
            assertThrows(SSTAFException.class, () -> {
                        String filename = "NotThere.json";
                        File file = makeFile(filename);
                        Human human = Human.from(file);
                        assertNull(human);
                    }
            );
        }

        @Test
        void badFileThrows() {
            assertThrows(SSTAFException.class, () -> {
                        String filename = "BadHuman1.json";
                        File file = makeFile(filename);
                        Human human = Human.from(file);
                        assertNull(human);
                    }
            );
        }
    }
}

