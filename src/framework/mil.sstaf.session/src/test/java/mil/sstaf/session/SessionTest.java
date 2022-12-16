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

package mil.sstaf.session;

import mil.sstaf.core.entity.*;
import mil.sstaf.session.control.EntityController;
import mil.sstaf.session.control.Session;
import mil.sstaf.session.control.SessionConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    private EntityController entityController;

    @BeforeEach
    void setup() {

        Unit bob = Unit.builder().name("Bob").build();
        Map<Force, List<BaseEntity>> map = Map.of(Force.BLUE, List.of(bob));

        entityController = EntityController.builder().entities(map).build();
        assertNotNull(entityController);

        var ub = Unit.builder();

    }

    @Test
    void newBuilder() {
        Assertions.assertDoesNotThrow(() -> {
            SessionConfiguration config = SessionConfiguration.builder().build();
            Session session = Session.of(config, entityController);
        });
    }

//    @Test
//    void submit() {
//        Session.ofBuilder bldr = Session.builder();
//        bldr.withController(entityController);
//        Session session = bldr.build();
//
//        Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
//        BaseSessionCommand cmd = new BaseSessionCommand(allPaths.iterator().next(), "This is a test");
//        session.submit(cmd);
//        assertEquals(1, entityController.getSessionProxyQueueDepth());
//    }
//
//    @Test
//    void tick() {
//        Session.Builder bldr = Session.builder();
//        bldr.withController(entityController);
//        Session session = bldr.build();
//
//        Collection<EntityHandle> allPaths = entityController.getSimulationEntityHandles();
//        BaseSessionCommand cmd = new BaseSessionCommand(allPaths.iterator().next(), "This is a test");
//        session.submit(cmd);
//
//        SessionTickResult tickResult = session.tick(10000);
//
//        List<BaseSessionResult> results = tickResult.getMessagesToClient();
//        assertEquals(1, results.size());
//        //
//        // The result should be an error because there are no handlers
//        // registered in the EntityController
//        //
//        Object x = results.get(0);
//        assertTrue(results.get(0) instanceof Error);
//        BaseSessionResult res = results.get(0);
//        if (res instanceof Error) {
//            Error error = (Error) res;
//            assertNull(error.getContent());
//            assertNotNull(error.getThrowable());
//        }
//    }
//
//    @Test
//    void getEntities() {
//        Session.Builder bldr = Session.builder();
//        bldr.withController(entityController);
//        Session session = bldr.build();
//        assertEquals(entityController.getSimulationEntityHandles().size(), session.getEntities().size());
//    }
}
