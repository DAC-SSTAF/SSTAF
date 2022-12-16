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

package mil.devcom_sc.ansur.handler;

import mil.devcom_sc.ansur.api.ANSURConfiguration;
import mil.devcom_sc.ansur.api.ANSURIIAnthropometry;
import mil.devcom_sc.ansur.api.constraints.Constraint;
import mil.devcom_sc.ansur.api.constraints.IntegerConstraint;
import mil.devcom_sc.ansur.messages.GetValueMessage;
import mil.devcom_sc.ansur.messages.GetValueResponse;
import mil.devcom_sc.ansur.messages.Sex;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.Loaders;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.Injector;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static mil.devcom_sc.ansur.messages.ValueKey.ACROMIAL_HEIGHT;
import static mil.devcom_sc.ansur.messages.ValueKey.SUBJECT_ID;
import static org.junit.jupiter.api.Assertions.*;

public class ANSURHandlerTest {
    public static String basepath = "src" + "/" + "test" + "/" + "resources" + "/";
    static ANSURConfiguration multiConstraintConfig;
    ANSURIIAnthropometry handler;

    @BeforeAll
    static void beginning() {
        multiConstraintConfig = new JsonLoader().load(Path.of(basepath, "multiple_constraints.json"), ANSURConfiguration.class);
    }


    @BeforeEach
    void setUp() {
        System.out.println(new File(".").getName());
        try {
            System.setProperty("sstaf.preloadFeatureClasses", "true");
            Loaders.preloadFeatureClasses("mil.devcom_sc.ansur.handler.ANSURIIHandler");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        Optional<ANSURIIAnthropometry> optAgent = Loaders.load(ANSURIIAnthropometry.class, "ANSUR Anthropometry", 0, 9);
        if (optAgent.isPresent()) {
            handler = optAgent.get();
        } else {
            Assertions.fail();
        }

        EntityHandle owner = EntityHandle.makeDummyHandle();
        Injector.inject(handler, owner);
    }


    @Nested
    @DisplayName("Tests that check normal operation")
    class HappyTests {

        @Test
        @DisplayName("Confirm that the ANSURHandler service can be loaded")
        void simpleTest() {
            assertNotNull(handler);
        }

        @Test
        @DisplayName("Confirm that multiple constraints can be applied")
        void multipleConstraintsWorks() {
            assertNotNull(handler);

            ANSURConfiguration ansurConfiguration = new JsonLoader().load(Path.of(basepath, "multiple_constraints.json"), ANSURConfiguration.class);

            assertDoesNotThrow(() -> {
                handler.configure(ansurConfiguration);
                handler.init();
                Optional<Integer> optVal = handler.getIntegerValue(SUBJECT_ID);
                if (optVal.isPresent()) {
                    Assertions.assertEquals(10303, optVal.get());
                } else {
                    Assertions.fail("Value for " + SUBJECT_ID.getHeaderLabel() + " was not found");
                }
            });
        }

        @Test
        @DisplayName("Confirm that an explicit SubjectID specification worksN")
        void subjectIDFromFileWorks() {
            assertNotNull(handler);

            assertDoesNotThrow(() -> {
                IntegerConstraint constraint = (IntegerConstraint) new JsonLoader().load(Path.of(basepath, "subjectID.json"));
                ANSURConfiguration config = ANSURConfiguration.builder().constraints(List.of(constraint)).build();
                assertNotNull(config);
                config.setSeed(1342345L);
                handler.configure(config);
                handler.init();
                Optional<Integer> optVal = handler.getIntegerValue(ACROMIAL_HEIGHT);
                if (optVal.isPresent()) {
                    Assertions.assertEquals(1335, optVal.get());
                } else {
                    Assertions.fail("Value for " + ACROMIAL_HEIGHT.getHeaderLabel() + " was not found");
                }
            });
        }


        @Test
        @DisplayName("Confirm that SubjectID formulated as a constraint works")
        void subjectIDAsConstraintWorks() {
            assertNotNull(handler);

            assertDoesNotThrow(() -> {
                IntegerConstraint constraint = (IntegerConstraint) new JsonLoader().load(Path.of(basepath, "subjectID_as_constraint.json"));
                ANSURConfiguration config = ANSURConfiguration.builder().constraints(List.of(constraint)).build();
                config.setSeed(1234234L);
                handler.configure(config);
                handler.init();
                Optional<Integer> optVal = handler.getIntegerValue(ACROMIAL_HEIGHT);
                if (optVal.isPresent()) {
                    Assertions.assertEquals(1335, optVal.get());
                } else {
                    Assertions.fail("Value for " + ACROMIAL_HEIGHT.getHeaderLabel() + " was not found");
                }
            });
        }


        @Test
        void subjectIDWorks() {
            assertNotNull(handler);

            assertDoesNotThrow(() -> {
                List<Constraint> constraints = List.of(IntegerConstraint.builder().propertyName(SUBJECT_ID.getHeaderLabel()).equals(10173).build());
                ANSURConfiguration configuration = ANSURConfiguration.builder().constraints(constraints).build();
                configuration.setSeed(12345L);
                handler.configure(configuration);
                handler.init();
                Optional<Integer> optVal = handler.getIntegerValue(ACROMIAL_HEIGHT);
                if (optVal.isPresent()) {
                    Assertions.assertEquals(1335, optVal.get());
                } else {
                    Assertions.fail("Value for " + ACROMIAL_HEIGHT.getHeaderLabel() + " was not found");
                }
            });

        }

        @Test
        @DisplayName("Confirm that at least one Female < 65kg can be found")
        void findLightFemales() {
            assertNotNull(handler);
            ANSURConfiguration ansurConfiguration = new JsonLoader().load(Path.of(basepath, "FemalesUnder65Kg.json"), ANSURConfiguration.class);

            assertDoesNotThrow(() -> {
                handler.configure(ansurConfiguration);
                handler.init();
                Optional<Integer> optVal = handler.getIntegerValue(SUBJECT_ID);
                Assertions.assertTrue(optVal.isPresent());
            });
        }

        @Test
        @DisplayName("Confirm that message handling works")
        void messageHandlingWorks() {
            assertNotNull(handler);

            ANSURConfiguration ansurConfiguration = new JsonLoader().load(Path.of(basepath, "multiple_constraints.json"), ANSURConfiguration.class);

            assertDoesNotThrow(() -> {
                handler.configure(ansurConfiguration);
                handler.init();

                GetValueMessage msg = GetValueMessage.of(ValueKey.BICEPS_CIRCUMFERENCE_FLEXED);
                Assertions.assertEquals(1, handler.contentHandled().size());
                Assertions.assertEquals(GetValueMessage.class, handler.contentHandled().get(0));

                ProcessingResult pr = handler.process(msg, 0, 0, Address.NOWHERE, 12345, Address.NOWHERE);
                Assertions.assertEquals(1, pr.messages.size());
                Message response = pr.messages.get(0);
                Object content = response.getContent();
                Assertions.assertTrue(content instanceof GetValueResponse);
                GetValueResponse gvr = (GetValueResponse) content;

                Optional<Integer> optVal = gvr.getIntegerValue();
                if (optVal.isPresent()) {
                    Assertions.assertEquals(341, optVal.get());
                } else {
                    Assertions.fail("Value for " + ValueKey.BICEPS_CIRCUMFERENCE_FLEXED.getHeaderLabel() + " was not found");
                }
            });
        }

        @DisplayName("Confirm that every ValueKey can be accessed")
        @ParameterizedTest(name = "[{index}] -- {0}")
        @EnumSource(ValueKey.class)
        void allValuesCanBeAccessed(ValueKey key) {
            assertNotNull(handler);

            assertDoesNotThrow(() -> {
                handler.configure(multiConstraintConfig);
                handler.init();

                Assertions.assertTrue(handler.getHeight_cm() > 0.0);
                assertEquals(handler.getSex(), Sex.MALE);
                Assertions.assertNotNull(handler.getName());
                Assertions.assertTrue(handler.getSpan_cm() > 0.0);
                Assertions.assertTrue(handler.getWeight_kg() > 0.0);

                GetValueMessage msg = GetValueMessage.of(key);
                Assertions.assertEquals(1, handler.contentHandled().size());
                Assertions.assertEquals(GetValueMessage.class, handler.contentHandled().get(0));

                ProcessingResult pr = handler.process(msg, 0, 0, Address.NOWHERE, 12345, Address.NOWHERE);
                Assertions.assertEquals(1, pr.messages.size());
                Message response = pr.messages.get(0);
                Object content = response.getContent();
                Assertions.assertTrue(content instanceof GetValueResponse);
                GetValueResponse gvr = (GetValueResponse) content;

                if (key.getType() == Integer.class) {
                    Optional<Integer> optVal1 = gvr.getIntegerValue();
                    Optional<Integer> optVal2 = handler.getIntegerValue(key);
                    Optional<String> optVal3 = handler.getStringValue(key);
                    if (optVal1.isEmpty() || optVal2.isEmpty()) {
                        Assertions.fail("Integer value for " + key.getHeaderLabel() + " was not found");
                    }
                    Assertions.assertEquals(optVal1.get(), optVal2.get());
                    Assertions.assertTrue(optVal3.isEmpty());
                } else if (key.getType() == String.class) {
                    Optional<String> optVal1 = gvr.getStringValue();
                    Optional<String> optVal2 = handler.getStringValue(key);
                    Optional<Integer> optVal3 = handler.getIntegerValue(key);
                    if (optVal1.isEmpty() || optVal2.isEmpty()) {
                        Assertions.fail("String value for " + key.getHeaderLabel() + " was not found");
                    }
                    Assertions.assertEquals(optVal1.get(), optVal2.get());
                    Assertions.assertTrue(optVal3.isEmpty());
                } else {
                    Assertions.fail(key.name() + " has bad type");
                }

                //
                // There are no double values, so this should always return an empty optional
                //
                Optional<Double> optionalDouble = handler.getDoubleValue(key);
                Assertions.assertTrue(optionalDouble.isEmpty());
            });
        }

    }

    @Nested
    @DisplayName("Test failure modes")
    class FailureTests {

        @Test
        @DisplayName("Confirm that if an unsupported HandleContent type is supplied then an exception is thrown")
        void badMessageThrows() {
            assertNotNull(handler);
            assertDoesNotThrow(() -> {
                List<Constraint> constraints = List.of(IntegerConstraint.builder().propertyName(SUBJECT_ID.getHeaderLabel()).equals(10173).build());
                ANSURConfiguration configuration = ANSURConfiguration.builder().constraints(constraints).build();
                configuration.setSeed(12345L);
                handler.configure(configuration);
                handler.init();
            });
            assertThrows(SSTAFException.class, () -> handler.process(HandlerContent.builder().build(), 0, 0, Address.NOWHERE, 0, Address.NOWHERE));
        }


    }
}


