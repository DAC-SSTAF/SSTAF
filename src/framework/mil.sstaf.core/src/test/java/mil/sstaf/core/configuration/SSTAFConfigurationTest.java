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

package mil.sstaf.core.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public class SSTAFConfigurationTest {


    public static final String PATH_STRING = "src/test/resources/moduleLayers";
    public static final Path SOURCE_FILE = Path.of(PATH_STRING, "config.json");

    @BeforeEach
    void b4each() {
        System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY, "");
        SSTAFConfiguration.reset();
    }

    @Nested
    @DisplayName("Confirm that the no-arg scenarios work")
    public class NoArgTests {

        @DisplayName("Confirm that invoking SSTAFContext.getRootLayer without init'ing returns the boot layer")
        @Test
        public void test0() {
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertEquals(ModuleLayer.boot(), ml);
        }

        @DisplayName("Check that system property -> SSTAFConfigurationTest1.json works")
        @Test
        public void test1() {
            System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                    "src/test/resources/moduleLayers/SSTAFConfigurationTest1.json");
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertNotNull(ml);

            var sl = ServiceLoader.load(ml, Predicate.class);
            long matches = sl.stream().count();
            Assertions.assertEquals(2, matches);
        }

        @DisplayName("Check that system property -> SSTAFConfigurationTest2.json works")
        @Test
        public void test2() {
            System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                    "src/test/resources/moduleLayers/SSTAFConfigurationTest2.json");
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertNotNull(ml);

            var sl = ServiceLoader.load(ml, Predicate.class);
            long matches = sl.stream().count();
            Assertions.assertEquals(3, matches);
        }

    }

    @Nested
    @DisplayName("Confirm that SSTAFContext can be initialized from a configuration file")
    public class JSONFileTests {

        @DisplayName("Use SSTAFConfigurationTest1.json")
        @Test
        public void test1() {
            System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                    "src/test/resources/moduleLayers/SSTAFConfigurationTest1.json");
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertNotNull(ml);

            var sl = ServiceLoader.load(ml, Predicate.class);
            long matches = sl.stream().count();
            Assertions.assertEquals(2, matches);
        }

        @DisplayName("Use SSTAFConfigurationTest2.json")
        @Test
        public void test2() {
            System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                    "src/test/resources/moduleLayers/SSTAFConfigurationTest2.json");
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertNotNull(ml);

            var sl = ServiceLoader.load(ml, Predicate.class);
            long matches = sl.stream().count();
            Assertions.assertEquals(3, matches);
        }

        @DisplayName("Use SSTAFConfigurationTest3.json")
        @Test
        public void test3() {
            System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                    "src/test/resources/moduleLayers/SSTAFConfigurationTest3.json");
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertNotNull(ml);

            var sl = ServiceLoader.load(ml, Predicate.class);
            long matches = sl.stream().count();
            Assertions.assertEquals(4, matches);
        }

        @DisplayName("Confirm that subclassing SSTAFConfiguration and loading it works automagically")
        @Test
        public void test4() {
            System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                    "src/test/resources/moduleLayers/SSTAFConfigurationTest4.json");
            ModuleLayer ml = SSTAFConfiguration.getInstance().getRootLayer();
            Assertions.assertNotNull(ml);
            SSTAFConfigurationToo sstafConfigurationToo = (SSTAFConfigurationToo)
                    SSTAFConfiguration.getInstance();

            Assertions.assertEquals("This is something more!", sstafConfigurationToo.getMore());

            var sl = ServiceLoader.load(ml, Predicate.class);
            long matches = sl.stream().count();
            Assertions.assertEquals(4, matches);

        }
    }

    @Nested
    @DisplayName("Confirm that SSTAFContext can be initialized from a JSONObject")
    public class JSONObjectTests {

        private JsonNode makeConfig(List<String> modulesList, List<String> pathList) {
            ObjectMapper om = new ObjectMapper();
            ObjectNode config = om.createObjectNode();

            ObjectNode moduleDef = om.createObjectNode();

            ArrayNode modules = om.createArrayNode();
            for (String module : modulesList) {
                modules.add(module);
            }

            ArrayNode paths = om.createArrayNode();
            for (String path : pathList) {
                paths.add(path);
            }

            moduleDef.set("modules", modules);
            moduleDef.set("modulePaths", paths);

            config.put("class", "mil.sstaf.core.configuration.SSTAFConfiguration");
            config.set("moduleLayerDefinition", moduleDef);

            return config;
        }

        private void check(int i) {
            SSTAFConfiguration config = SSTAFConfiguration.getInstance();
            Assertions.assertNotNull(config.getRootLayer());
        }

        @DisplayName("Check mil.sstaftest.fred")
        @Test
        public void test1A() {
            try {
                SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.fred"),
                                List.of("dir1")),
                        SOURCE_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @DisplayName("Check mil.sstaftest.barney")
        @Test
        public void test1B() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.barney"),
                            List.of("dir2")),
                    SOURCE_FILE);
            check(1);
        }

        @DisplayName("Check mil.sstaftest.wilma")
        @Test
        public void test1C() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.wilma"),
                            List.of("dir3")),
                    SOURCE_FILE);
            check(1);
        }

        @DisplayName("Check mil.sstaftest.betty")
        @Test
        public void test1D() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.betty"),
                            List.of("dir4")),
                    SOURCE_FILE);
            check(1);
        }

        @DisplayName("Check fred and barney")
        @Test
        public void test2A() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.fred", "mil.sstaftest.barney"),
                            List.of("dir1", "dir2")),
                    SOURCE_FILE);
            check(2);
        }

        @DisplayName("Check fred and wilma")
        @Test
        public void test2B() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.fred", "mil.sstaftest.wilma"),
                            List.of("dir1", "dir3")),
                    SOURCE_FILE);
            check(2);
        }

        @DisplayName("Check fred and betty")
        @Test
        public void test2C() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.fred", "mil.sstaftest.betty"),
                            List.of("dir1", "dir4")),
                    SOURCE_FILE);
            check(2);
        }

        @DisplayName("Check barney and wilma")
        @Test
        public void test2D() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.barney", "mil.sstaftest.wilma"),
                            List.of("dir2", "dir3")),
                    SOURCE_FILE);
            check(2);
        }

        @DisplayName("Check barney and betty")
        @Test
        public void test2E() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.barney", "mil.sstaftest.betty"),
                            List.of("dir2", "dir4")),
                    SOURCE_FILE);
            check(2);
        }

        @DisplayName("Check wilma and betty")
        @Test
        public void test2F() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.wilma", "mil.sstaftest.betty"),
                            List.of("dir3", "dir4")),
                    SOURCE_FILE);
            check(2);
        }

        @DisplayName("Check all")
        @Test
        public void test2G() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.wilma", "mil.sstaftest.betty",
                                    "mil.sstaftest.fred", "mil.sstaftest.barney"),
                            List.of("dir1", "dir2",
                                    "dir3", "dir4")),
                    SOURCE_FILE);
            check(4);
        }

        @DisplayName("Check all - scrambled dirs")
        @Test
        public void test2H() {
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.wilma", "mil.sstaftest.betty",
                                    "mil.sstaftest.fred", "mil.sstaftest.barney"),
                            List.of("dir4", "dir3",
                                    "dir2", "dir1")),
                    SOURCE_FILE);
            check(4);
        }

        @DisplayName("Check all - scrambled dirs, absolute paths")
        @Test
        public void test2I() {
            List<String> relatives = List.of("src/test/resources/moduleLayers/dir4", "src/test/resources/moduleLayers/dir3",
                    "src/test/resources/moduleLayers/dir2", "src/test/resources/moduleLayers/dir1");
            String cwd = System.getProperty("user.dir");
            List<String> absolutes = new ArrayList<>();
            for (String s : relatives) {
                absolutes.add(cwd + File.separator + s);
            }
            SSTAFConfiguration.from(makeConfig(List.of("mil.sstaftest.wilma", "mil.sstaftest.betty",
                            "mil.sstaftest.fred", "mil.sstaftest.barney"), absolutes),
                    SOURCE_FILE);
            check(4);
        }

    }
}



