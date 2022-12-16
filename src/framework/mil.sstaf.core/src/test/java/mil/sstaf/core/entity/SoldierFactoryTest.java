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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SoldierFactoryTest {


    static Path USER_DIR = Path.of(System.getProperty("user.dir"));
    static Path RESOURCE_DIR = Path.of(USER_DIR.toString(), "src/test/resources/SoldierFactoryTest");

    private File makeFile(String filename) {
        Path path = Path.of(RESOURCE_DIR.toString(), filename);
        return path.toFile();
    }
    
    @Nested
    @DisplayName("Test the Happy Path where everything works")
    class HappyTests {

        @Test
        @DisplayName("Confirm that a file that uses only references resolves correctly")
        void allReferenceJSONFileWorks() {
            String filename = "TestSoldier1.json";
            Soldier soldier = Soldier.from(makeFile(filename));
            assertNotNull(soldier);
            assertEquals(Rank.E6, soldier.getRank());
        }

        @Test
        @DisplayName("Confirm that good JSON works")
        void goodJSONWorks() {
            ObjectMapper om = new ObjectMapper();
            ObjectNode soldier1 = om.createObjectNode();
            soldier1.put("class", "mil.sstaf.core.entity.Soldier");
            soldier1.put("name", "Test Dude");
            soldier1.put("rank", "E6");


            soldier1.set("features", om.createArrayNode());
            soldier1.set("configurations", om.createObjectNode());

            Soldier soldier = Soldier.from(soldier1, Path.of(RESOURCE_DIR.toString(), "soldier.json"));
            assertNotNull(soldier);
            assertEquals(Rank.E6, soldier.getRank());
        }

    }

    @Nested
    @DisplayName("Check the failure modes")
    class UnhappyTests {

        @Test
        @DisplayName("Confirm that a file with bad JSON throws")
        void badJSONThrows() {
            assertThrows(SSTAFException.class, () -> {
                ObjectMapper om = new ObjectMapper();
                Soldier soldier = Soldier.from(om.createObjectNode(), Path.of(RESOURCE_DIR.toString(), "soldier.json"));
                assertNotNull(soldier);
            });
        }

        @Test
        @DisplayName("Confirm that a missing file throws")
        void missingFileThrows() {
            assertThrows(SSTAFException.class, () -> {
                        String filename = "NotThere.json";
                        Soldier soldier = Soldier.from(makeFile(filename));
                        assertNull(soldier);
                    }
            );
        }

        @Test
        @DisplayName("Confirm that a file with missing stuff throws")
        void badFileThrows() {
            assertThrows(SSTAFException.class, () -> {
                        String filename = "BadSoldier1.json";
                        Soldier soldier = Soldier.from(makeFile(filename));
                        assertNull(soldier);
                    }
            );
        }

        @Test
        @DisplayName("Confirm that a file with missing Antho throws")
        void missingAnthroThrows() {
            assertThrows(SSTAFException.class, () -> {
                        String filename = "BadSoldier2.json";
                        Soldier soldier = Soldier.from(makeFile(filename));
                        assertNull(soldier);
                    }
            );
        }
    }

}

