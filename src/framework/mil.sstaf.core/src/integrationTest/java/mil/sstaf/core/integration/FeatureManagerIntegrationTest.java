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

import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.entity.FeatureManager;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.FeatureSpecification;
import mil.sstaf.core.features.IntContent;
import mil.sstaf.core.features.StringContent;
import mil.sstaf.core.module.ModuleLayerDefinition;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeatureManagerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(FeatureManagerIntegrationTest.class);

    @BeforeEach
    public void setup() {
        System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                "src" + File.separator +
                        "integrationTest" + File.separator +
                        "resources" + File.separator +
                        "EmptyConfiguration.json");
    }

    @Nested
    @DisplayName("Basic Tests")
    public class InnerOne {
        @Test
        @DisplayName("Confirm that the FeatureManager can load a Feature graph from a Handler specification")
        void canLoadAHandlerBySpec() {
            FeatureSpecification spec = FeatureSpecification.builder()
                    .featureName("Handler1")
                    .majorVersion(3).minorVersion(1).requireExact(false).build();
            EntityHandle entityHandle = EntityHandle.makeDummyHandle();
            List<FeatureSpecification> specificationList = List.of(spec);
            Map<String, FeatureConfiguration> configurations = new HashMap<>();
            FeatureManager featureManager = new FeatureManager(entityHandle, specificationList, configurations, 3);
            featureManager.init();
            assertTrue(featureManager.getSpecificationForHandler(StringContent.class).isPresent());
        }

        @Test
        @DisplayName("Confirm that the FeatureManager can load a Feature graph from an Agent specification")
        void canLoadAnAgentBySpec() {
            FeatureSpecification spec = FeatureSpecification.builder()
                    .featureName("Agent1")
                    .majorVersion(3).minorVersion(1).requireExact(false).build();
            EntityHandle entityHandle = EntityHandle.makeDummyHandle();
            List<FeatureSpecification> specificationList = List.of(spec);
            Map<String, FeatureConfiguration> configurations = new HashMap<>();
            FeatureManager featureManager = new FeatureManager(entityHandle, specificationList, configurations, 3);
            featureManager.init();
            assertTrue(featureManager.getSpecificationForHandler(IntContent.class).isPresent());
        }
    }

    @Nested
    @DisplayName("Test ModuleLayer support")
    public class InnerTwo {

        @Test
        @DisplayName("Confirm that module and path lists work")
        public void test1() {

            FeatureSpecification spec = FeatureSpecification.builder()
                    .featureName("Pinky")
                    .majorVersion(13).minorVersion(0).requireExact(false).build();
            EntityHandle entityHandle = EntityHandle.makeDummyHandle();
            List<FeatureSpecification> specificationList = List.of(spec);
            Map<String, FeatureConfiguration> configurations = new HashMap<>();
            FeatureManager featureManager = null;
            Set<Path> paths = Set.of(Path.of("src/integrationTest/resources/modules/pinky"));
            Set<String> modules = Set.of("mil.sstaftest.mocks.pinky");

            ModuleLayerDefinition moduleLayerDefinition =
                    ModuleLayerDefinition.builder()
                            .modulePaths(paths)
                            .modules(modules)
                            .build();

            featureManager = new FeatureManager(entityHandle, moduleLayerDefinition, specificationList, configurations, 3);
            featureManager.init();
            assertNotNull(featureManager);
            assertTrue(featureManager.getSpecificationForHandler(StringContent.class).isPresent());

        }

        @Test
        @DisplayName("Confirm that an exception is thrown when the module isn't found")
        public void test2() {
            Assertions.assertThrows(SSTAFException.class, () -> {
                FeatureSpecification spec = FeatureSpecification.builder()
                        .featureName("Pinky")
                        .majorVersion(13).minorVersion(0).requireExact(false).build();
                EntityHandle entityHandle = EntityHandle.makeDummyHandle();
                List<FeatureSpecification> specificationList = List.of(spec);
                Map<String, FeatureConfiguration> configurations = new HashMap<>();

                Set<Path> paths = Set.of(Path.of("src/integrationTest/resources/modules/pinky"));
                Set<String> modules = Set.of("mil.sstaftest.mocks.notreal");

                ModuleLayerDefinition moduleLayerDefinition =
                        ModuleLayerDefinition.builder()
                                .modulePaths(paths)
                                .modules(modules)
                                .build();

                FeatureManager featureManager = new FeatureManager(entityHandle, moduleLayerDefinition, specificationList, configurations, 3);
                featureManager.init();
                assertTrue(featureManager.getSpecificationForHandler(StringContent.class).isPresent());
            });
        }

        @Test
        @DisplayName("Confirm that transitive resolution works")
        public void test3() {

            Map<String, FeatureConfiguration> configurations = new HashMap<>();
            FeatureConfiguration sc = FeatureConfiguration.builder().build();
            configurations.put("Echo", sc);
            configurations.put("Delta", sc);
            configurations.put("Charlie", sc);
            configurations.put("Bravo", sc);
            configurations.put("Alpha", sc);
            configurations.put("James Bond", sc);

            Assertions.assertDoesNotThrow(() -> {
                FeatureSpecification spec = FeatureSpecification.builder()
                        .featureName("James Bond")
                        .majorVersion(7).minorVersion(0).requireExact(false).build();
                EntityHandle entityHandle = EntityHandle.makeDummyHandle();
                List<FeatureSpecification> specificationList = List.of(spec);

                String userDir = System.getProperty("user.dir");

                Set<Path> paths = Set.of(
                        Path.of(userDir, "../../testFeatures/integration/mil.sstaftest.alpha/build/libs"),
                        Path.of(userDir, "../../testFeatures/integration/mil.sstaftest.bravo/build/libs"),
                        Path.of(userDir, "../../testFeatures/integration/mil.sstaftest.charlie/build/libs"),
                        Path.of(userDir, "../../testFeatures/integration/mil.sstaftest.delta/build/libs"),
                        Path.of(userDir, "../../testFeatures/integration/mil.sstaftest.echo/build/libs"),
                        Path.of(userDir, "../../testFeatures/integration/mil.sstaftest.jamesbond/build/libs"));

                Set<String> modules = Set.of("mil.sstaftest.jamesbond");

                ModuleLayerDefinition moduleLayerDefinition =
                        ModuleLayerDefinition.builder()
                                .modulePaths(paths)
                                .modules(modules)
                                .build();
                FeatureManager featureManager = new FeatureManager(entityHandle, moduleLayerDefinition,
                        specificationList, configurations, 3);
                featureManager.init();
            });
        }

    }

}

