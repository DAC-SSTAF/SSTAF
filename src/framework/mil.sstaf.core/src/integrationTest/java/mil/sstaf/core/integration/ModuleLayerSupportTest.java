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

import mil.sstaf.core.module.ModuleLayerDefinition;
import mil.sstaf.core.module.ModuleLayerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleLayerSupportTest {

    private ModuleLayer pl;
    private Path dir1;
    private Path dir2;

    @BeforeEach
    void before() {
        pl = ModuleLayer.boot();
        String dirBase = "src" + File.separator
                + "test" + File.separator
                + "resources" + File.separator;
        dir1 = Path.of("src" ,"integrationTest", "resources", "modules", "jamesbond");
        dir2 = Path.of("src" ,"integrationTest", "resources", "modules", "pinky");
    }

    @Nested
    @DisplayName("Test the happy path")
    class HappyTests {

        @Test
        @DisplayName("Confirm that if no modules are provided the parent layer is returned")
        public void fallbackToParentLayer1() {
            ModuleLayerDefinition moduleLayerDefinition =
                    ModuleLayerDefinition.builder().build();
            ModuleLayer ml = ModuleLayerSupport.makeModuleLayer(pl,
                    moduleLayerDefinition,
                    this.getClass().getClassLoader());
            assertEquals(pl, ml);
            //fail("Bob");
        }

        @Test
        @DisplayName("Confirm a new ModuleLayer can be made.")
        public void npn() {
            String dirBase = "src" + File.separator
                    + "test" + File.separator
                    + "resources" + File.separator;
            Set<Path> paths = Set.of(dir1);
            Set<String> modules = Set.of("mil.sstaftest.jamesbond");
            ModuleLayerDefinition moduleLayerDefinition =
                    ModuleLayerDefinition.builder()
                            .modulePaths(paths)
                            .modules(modules)
                            .build();
            ModuleLayer ml = ModuleLayerSupport.makeModuleLayer(pl,
                    moduleLayerDefinition,
                    this.getClass().getClassLoader());
            assertNotEquals(pl, ml);
            //fail("Bob");
        }

    }

}

