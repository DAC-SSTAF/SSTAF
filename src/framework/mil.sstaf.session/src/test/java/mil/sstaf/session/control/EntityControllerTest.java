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

package mil.sstaf.session.control;

import mil.sstaf.core.entity.*;
import mil.sstaf.core.features.ExceptionContent;
import mil.sstaf.core.features.StringContent;
import mil.sstaf.session.messages.Error;
import mil.sstaf.session.messages.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityControllerTest {

    private EntityController entityController;
    private Unit unit;

    @BeforeEach
    void setup() {
        var ub = Unit.builder();
        ub.name("Bob");
        unit = ub.build();
        unit.init();

        var ecb = EntityController.builder();
        assertNotNull(ecb);
        ecb.entities(Map.of(Force.BLUE, List.of(unit)));

        entityController = ecb.build();
    }


    @Nested
    @DisplayName("Test basic scenarios")
    class BasicTests {

        @Test
        @DisplayName("Confirm that the EntityController is built successfully")
        void testControllerBuilder() {
            assertNotNull(entityController);
        }

        @Test
        @DisplayName("Confirm that a BaseSessionCommand can be submitted and the queue holds it")
        void testSubmitCommand() {
            Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
            assertEquals(1, allPaths.size());
            Command cmd = Command.builder().
                    recipientPath(allPaths.iterator().next().getPath())
                    .content(StringContent.builder().value("This is a test").build())
                    .build();
            entityController.submitCommand(cmd);
            assertEquals(1, entityController.getSessionProxyQueueDepth());
        }

        @Test
        @DisplayName("Confirm that a BaseSessionCommand can be submitted and the queue holds it")
        void testSubmitEvent() {
            Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
            Event event = Event.builder()
                    .recipientPath(allPaths.iterator().next().getPath())
                    .content(StringContent.builder().value("This is a test").build())
                    .eventTime_ms(10000)
                    .build();
            entityController.submitEvent(event);
            assertEquals(1, entityController.getSessionProxyQueueDepth());
        }

        @Test
        @DisplayName("Confirm that getSimulationEntityHandles() works as expected")
        void getAllEntityHandles() {
            Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
            assertEquals(1, allPaths.size());
        }

        @Test
        @DisplayName("Confirm that getLastTimeTime_ms works")
        void testTick() {
            Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
            Command cmd = Command.builder()
                    .recipientPath(allPaths.iterator().next().getForcePath())
                    .content(StringContent.builder().value("This is a test").build())
                    .build();
            entityController.submitCommand(cmd);
            assertEquals(1, entityController.getSessionProxyQueueDepth());

            int currentTime_ms = 10000;
            SessionTickResult tickResult = entityController.tick(currentTime_ms);
            assertNotNull(tickResult);
            assertEquals(currentTime_ms, entityController.getLastTickTime_ms());
        }
    }

    @Nested
    @DisplayName("Test failure modes")
    class FailureTests {
        @Test
        @DisplayName("Confirm that dispatching a message to an entity without an appropriate handle produces and error message")
        void testTick() {
            Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
            Command cmd = Command.builder()
                    .recipientPath(allPaths.iterator().next().getForcePath())
                    .content( StringContent.builder().value("This is a test").build())
                    .build();
            entityController.submitCommand(cmd);
            assertEquals(1, entityController.getSessionProxyQueueDepth());

            //
            // Spoon!
            //
            SessionTickResult tickResult = entityController.tick(10000);
            assertEquals(1, tickResult.getMessagesToClient().size());
            BaseSessionResult out = tickResult.getMessagesToClient().get(0);
            //
            // The result should be an error because there are no handlers
            // registered in Bob.
            //
            assertTrue(out instanceof Error);
        }

        @Test
        @DisplayName("Confirm that an error message can be converted to a result.")
        void testConvertErrorMessageToResult() {
            var b = ErrorResponse.builder()
                    .sequenceNumber(1234)
                    .messageID(3)
                    .errorDescription("It's broken")
                    .destination(Address.makeExternalAddress(unit.getHandle()))
                    .source(Address.makeExternalAddress(unit.getHandle()))
                    .content(ExceptionContent.builder().thrown(new Throwable()).build());
            ErrorResponse er = b.build();
            BaseSessionResult sr = entityController.convertMessageToResult(er);
            assertNotNull(sr);
            assertTrue(sr instanceof Error);
            assertEquals("BLUE" + Entity.ENTITY_PATH_DELIMITER + "Bob", ((Error)sr).getEntityPath());
        }
    }


    @Nested
    @DisplayName("EntityController.Factory tests")
    class ECTTests {

        @Test
        void testSimpleParseAndBuild() {

            File file = new File("src/test/resources/EntityControllerFactoryTest/EntityConfig1.json");
            EntityController entityController = EntityController.from(file);
            Assertions.assertNotNull(entityController);
            Assertions.assertTrue(entityController.getId() >= MessageDriven.BlockCounter.SYSTEM_BLOCK_BEGIN);
            Assertions.assertTrue(entityController.getId() < MessageDriven.BlockCounter.USER_BLOCK_BEGIN);
        }

        @Test
        void testParseAndBuild1FireTeam() {
            File file = new File("src/test/resources/EntityControllerFactoryTest/EntityConfig1FireTeam.json");
            EntityController entityController = EntityController.from(file);
            Assertions.assertNotNull(entityController);
            Assertions.assertTrue(entityController.getId() >= MessageDriven.BlockCounter.SYSTEM_BLOCK_BEGIN);
            Assertions.assertTrue(entityController.getId() < MessageDriven.BlockCounter.USER_BLOCK_BEGIN);
            Assertions.assertEquals(5, entityController.getSimulationEntityHandles().size());
        }

        @Test
        void testParseAndBuild1Squad() {
            File file = new File("src/test/resources/EntityControllerFactoryTest/EntityConfig1Squad.json");
            EntityController entityController = EntityController.from(file);
            Assertions.assertNotNull(entityController);
            Assertions.assertTrue(entityController.getId() >= MessageDriven.BlockCounter.SYSTEM_BLOCK_BEGIN);
            Assertions.assertTrue(entityController.getId() < MessageDriven.BlockCounter.USER_BLOCK_BEGIN);
            Assertions.assertEquals(12, entityController.getSimulationEntityHandles().size());
        }

        @Test
        void testParseAndBuild1Platoon() {
            File file = new File(
                    "src/test/resources/EntityControllerFactoryTest/EntityConfig1Platoon.json");
            EntityController entityController = EntityController.from(file);
            Assertions.assertNotNull(entityController);
            Assertions.assertTrue(entityController.getId() >= MessageDriven.BlockCounter.SYSTEM_BLOCK_BEGIN);
            Assertions.assertTrue(entityController.getId() < MessageDriven.BlockCounter.USER_BLOCK_BEGIN);
            Assertions.assertEquals(39, entityController.getSimulationEntityHandles().size());
        }

        @Test
        void testParseAndBuild10Platoons() {
            File file = new File(
                    "src/test/resources/EntityControllerFactoryTest/EntityConfig10Platoons.json");
            EntityController entityController = EntityController.from(file);
            Assertions.assertNotNull(entityController);
            Assertions.assertTrue(entityController.getId() >= MessageDriven.BlockCounter.SYSTEM_BLOCK_BEGIN);
            Assertions.assertTrue(entityController.getId() < MessageDriven.BlockCounter.USER_BLOCK_BEGIN);
            Assertions.assertEquals(390, entityController.getSimulationEntityHandles().size());

        }
    }
}
