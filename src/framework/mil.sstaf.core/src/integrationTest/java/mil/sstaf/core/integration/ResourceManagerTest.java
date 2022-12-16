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

package mil.sstaf.core.integration;

import mil.sstaf.core.features.ResourceManager;
import mil.sstaftest.simplemock.SimpleMockFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceManagerTest {

    @Nested
    @DisplayName("ResourceManager tests that use the assembled jar")
    class ITests {

        @Test
        @DisplayName(value = "Confirm that providing a null resource owner throws NullPointerException")
        public void nullResourceOwnerThrows() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                Path tempDir = Files.createTempDirectory("Banana");
                ResourceManager.extractResource(null, "extractionTest/extractMe.txt", tempDir);
            });
        }

        @Test
        @DisplayName(value = "Confirm that providing a null resource name throws NullPointerException")
        public void nullResourceNameThrows() {
            Assertions.assertThrows(NullPointerException.class, () -> {
                Path tempDir = Files.createTempDirectory("Banana");
                ResourceManager.extractResource(this.getClass(), null, tempDir);
            });
        }

        @Test
        @DisplayName(value = "Confirm that providing a null directory throws NullPointerException")
        public void nullDirectoryThrows() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> ResourceManager.extractResource(this.getClass(),
                            "extractionTest/extractMe.txt", null));
        }


        @Test
        @DisplayName("Confirm that the constructor works and the checksum table can be read")
        public void constructorTest() {
            ResourceManager rm = ResourceManager.getManager(SimpleMockFeature.class);
            Assertions.assertEquals(2, rm.getResourceFiles().size());
            Assertions.assertEquals(2, rm.getNumWritten());
            Assertions.assertTrue(rm.getDirectory().toString().contains("SimpleMockFeature"));
        }

        @Test
        @DisplayName("Confirm that reusing the same directory results in no files being written")
        public void reusedDirTest() {
            ResourceManager rm = ResourceManager.getManager(SimpleMockFeature.class);
            Path where = rm.getDirectory();
            assertEquals(2, rm.getNumWritten());

            ResourceManager rm2 = ResourceManager.getManager(SimpleMockFeature.class, where);
            assertEquals(where.toString(), rm2.getDirectory().toString());
            assertEquals(0, rm2.getNumWritten());

            Map<String, File> map = rm2.getResourceFiles();
            for (Map.Entry<String, File> entry : map.entrySet()) {
                assertTrue(entry.getValue().exists());
                assertTrue(entry.getValue().canRead());
                assertTrue(entry.getValue().getPath().contains(entry.getKey()));
            }
        }
    }
}

