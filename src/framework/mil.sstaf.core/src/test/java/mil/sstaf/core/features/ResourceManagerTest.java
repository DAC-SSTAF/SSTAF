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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceManagerTest {

    @Nested
    @DisplayName("ResourceManager tests that don't require the assembled jar")
    class UnitTests {
        @Test
        @DisplayName(value = "Confirm that a resource at the top level can be extracted")
        public void extractionWork1() {
            try {
                Path tempDir = Files.createTempDirectory("Banana");
                File resourceFile = ResourceManager.extractResource(this.getClass(), "topLevelResource.txt", tempDir);
                assertNotNull(resourceFile);
                assertTrue(resourceFile.exists());
            } catch (IOException e) {
                e.printStackTrace();
                Assertions.fail("IO!");
            }
        }

        @Test
        @DisplayName(value = "Confirm that a resource in a subdirectory can be extracted")
        public void extractionWorks2() {
            try {
                Path tempDir = Files.createTempDirectory("Banana");
                File resourceFile = ResourceManager.extractResource(this.getClass(), "extractionTest/extractMe.txt", tempDir);
                assertNotNull(resourceFile);
                assertTrue(resourceFile.exists());
            } catch (IOException e) {
                e.printStackTrace();
                Assertions.fail("IO!");
            }
        }

        @Test
        @DisplayName(value = "Confirm that attempting to extract a non-existent resource throws IOException")
        public void badResourceThrows() {
            Assertions.assertThrows(IOException.class, () -> {
                Path tempDir = Files.createTempDirectory("Banana");
                ResourceManager.extractResource(this.getClass(), "extractionTest/nonExistent.txt", tempDir);
            });
        }

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


    }
}

