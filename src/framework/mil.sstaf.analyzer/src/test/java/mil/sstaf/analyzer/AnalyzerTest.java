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

import mil.sstaf.analyzer.messages.BaseAnalyzerCommand;
import mil.sstaf.analyzer.messages.Tick;
import mil.sstaf.session.control.Session;
import mil.sstaf.session.control.SessionConfiguration;
import mil.sstaf.session.control.EntityController;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;


@Disabled
public class AnalyzerTest {

    public static final String END_SESSION_MSG = "{ \"class\" : \"mil.sstaf.analyzer.commands.EndSession\" }\n";

    private String mockStringSupplier() {
        return "";
    }

    private BaseAnalyzerCommand mockDeserializer(String s) {
        return Tick.builder().time_ms(0).build();
    }

    private void mockStringConsumer(String out) {

    }

    private String mockSerializer(Object o) {
        return "";
    }

    static class StringSupplier implements Supplier<String> {

        Deque<String> queue = new ArrayDeque<>(20);

        void add(String s) {
            queue.offer(s);
        }

        @Override
        public String get() {
            return queue.removeFirst();
        }
    }


    static class StringConsumer implements Consumer<String> {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> stuff = new ArrayList<>();
        @Override
        public void accept(String s) {
            stringBuilder.append(s);
            stringBuilder.append("\n");
            stuff.add(stringBuilder.toString());
        }
    }

    @Nested
    @DisplayName("Test normal behavior (The Happy Path)")
    class HappyTests {
        @Test
        @DisplayName("Confirm an Analyzer can be created")
        void testCreation() {
            File entityFile = new File("src/test/resources/analyzerTest/entityConfig.json");
            EntityController entityController = EntityController.from(entityFile);
            SessionConfiguration configuration = SessionConfiguration
                    .builder()
                    .async(false)
                    .build();
            Session session = Session.of(configuration, entityController);
            assertDoesNotThrow(() -> new Analyzer(
                    session,
                    AnalyzerTest.this::mockStringSupplier,
                    AnalyzerTest.this::mockDeserializer,
                    AnalyzerTest.this::mockSerializer,
                    AnalyzerTest.this::mockStringConsumer));
        }

        @Test
        @DisplayName("EndSession works")
        void testEndSession() throws InterruptedException, ExecutionException {
            StringSupplier sup = new StringSupplier();
            StringConsumer con = new StringConsumer();

            sup.add(END_SESSION_MSG);

            Analyzer analyzer = makeAnalyzer(sup, con);
            assertEquals(0, analyzer.getTaskCount());
            assertFalse(analyzer.isRunning());
            analyzer.start();
            sleep(1000);
            assertFalse(analyzer.isRunning());
            assertEquals(1, analyzer.getTaskCount());
            assertTrue(sup.queue.isEmpty());
        }

        @Test
        @DisplayName("Tick works")
        void testTickSession() throws InterruptedException, ExecutionException {
            StringSupplier sup = new StringSupplier();
            StringConsumer con = new StringConsumer();

            String tick = "{ \"class\" : \"mil.sstaf.analyzer.commands.Tick\", \"time_ms\" : 10000 }\n";
            sup.add(tick);
            sup.add(END_SESSION_MSG);

            Analyzer analyzer = makeAnalyzer(sup, con);
            assertEquals(0, analyzer.getTaskCount());
            assertFalse(analyzer.isRunning());
            analyzer.start();
            sleep(1000);
            assertFalse(analyzer.isRunning());
            assertEquals(2, analyzer.getTaskCount());
            assertTrue(sup.queue.isEmpty());
        }



        private Analyzer makeAnalyzer(Supplier<String> in, Consumer<String> out) {
            File entityFile = new File("src/test/resources/analyzerTest/entityConfig.json");
            Analyzer analyzer = getAnalyzer(in, out, entityFile);
            return analyzer;
        }

        private Analyzer makePlatoonAnalyzer(Supplier<String> in, Consumer<String> out) {
            File entityFile = new File("../../testInput/goodEntityFiles/OneSoldier.json");
            Analyzer analyzer = getAnalyzer(in, out, entityFile);
            return analyzer;
        }
    }

    private Analyzer getAnalyzer(Supplier<String> in, Consumer<String> out, File entityFile) {
        EntityController entityController = EntityController.from(entityFile);
        SessionConfiguration configuration = SessionConfiguration
                .builder()
                .async(false)
                .build();
        Session session = Session.of(configuration, entityController);
        Analyzer analyzer = new Analyzer(session,
                in,
                new JsonDeserializer(),
                new JsonSerializer(),
                out);
        assertNotNull(analyzer);
        return analyzer;
    }

    @Nested
    @DisplayName("Test failure behavior (The Unhappy Paths")
    class UnhappyTests {
        @Test
        @DisplayName("Confirm null session throws")
        void testNullSession() {
            assertThrows(NullPointerException.class, () -> new Analyzer(null,
                    AnalyzerTest.this::mockStringSupplier,
                    AnalyzerTest.this::mockDeserializer,
                    AnalyzerTest.this::mockSerializer,
                    AnalyzerTest.this::mockStringConsumer));
        }

        @Test
        @DisplayName("Confirm null string source fails throws")
        void testNullSource() {
            assertThrows(NullPointerException.class, () -> {
                File entityFile = new File("src/test/resources/analyzerTest/entityConfig.json");
                EntityController entityController = EntityController.from(entityFile);
                SessionConfiguration configuration = SessionConfiguration
                        .builder()
                        .async(false)
                        .build();
                try (Session session = Session.of(configuration, entityController)) {
                    Analyzer analyzer = new Analyzer(session,
                            null,
                            AnalyzerTest.this::mockDeserializer,
                            AnalyzerTest.this::mockSerializer,
                            AnalyzerTest.this::mockStringConsumer
                    );
                    assertNotNull(analyzer);
                }
            });
        }

        @Test
        @DisplayName("Confirm that a null converter throws")
        void testNullConverter() {
            assertThrows(NullPointerException.class, () -> {
                File entityFile = new File("src/test/resources/analyzerTest/entityConfig.json");
                EntityController entityController = EntityController.from(entityFile);
                SessionConfiguration configuration = SessionConfiguration
                        .builder()
                        .async(false)
                        .build();
                try (Session session = Session.of(configuration, entityController)) {
                    Analyzer analyzer = new Analyzer(session,
                            AnalyzerTest.this::mockStringSupplier,
                            null,
                            AnalyzerTest.this::mockSerializer,
                            AnalyzerTest.this::mockStringConsumer
                    );
                    assertNotNull(analyzer);
                }
            });
        }

        @Test
        @DisplayName("Confirm that a null serializer throws")
        void testNullSerializer() {
            assertThrows(NullPointerException.class, () -> {
                File entityFile = new File("src/test/resources/analyzerTest/entityConfig.json");
                EntityController entityController = EntityController.from(entityFile);
                SessionConfiguration configuration = SessionConfiguration
                        .builder()
                        .async(false)
                        .build();
                try (Session session = Session.of(configuration, entityController)) {
                    Analyzer analyzer = new Analyzer(session,
                            AnalyzerTest.this::mockStringSupplier,
                            AnalyzerTest.this::mockDeserializer,
                            null,
                            AnalyzerTest.this::mockStringConsumer
                    );
                    assertNotNull(analyzer);
                }
            });
        }

        @Test
        @DisplayName("Confirm that a null String consumer throws")
        void testNullDestination() {
            assertThrows(NullPointerException.class, () -> {
                File entityFile = new File("src/test/resources/analyzerTest/entityConfig.json");
                EntityController entityController = EntityController.from(entityFile);
                SessionConfiguration configuration = SessionConfiguration
                        .builder()
                        .async(false)
                        .build();
                try (Session session = Session.of(configuration, entityController)) {
                    Analyzer analyzer = new Analyzer(session,
                            AnalyzerTest.this::mockStringSupplier,
                            AnalyzerTest.this::mockDeserializer,
                            AnalyzerTest.this::mockSerializer,
                            null
                    );
                    assertNotNull(analyzer);
                }
            });
        }
    }

}

