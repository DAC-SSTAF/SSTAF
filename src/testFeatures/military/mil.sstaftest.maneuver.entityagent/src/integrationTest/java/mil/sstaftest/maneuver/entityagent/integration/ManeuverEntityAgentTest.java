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

package mil.sstaftest.maneuver.entityagent.integration;

import lombok.experimental.SuperBuilder;
import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.entity.*;
import mil.sstaf.core.features.*;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.util.Injector;
import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaftest.maneuver.api.*;
import mil.sstaftest.maneuver.entityagent.ManeuverEntityAgent;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManeuverEntityAgentTest {
    Logger logger = LoggerFactory.getLogger(ManeuverEntityAgentTest.class);

    private Bob bob;
    private ManeuverEntityAgent agent;
    private Blackboard blackboard;

    @BeforeEach
    void setup() {
        logger.info("Beginning setup");

        System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
                "src" + File.separator +
                        "integrationTest" + File.separator +
                        "resources" + File.separator +
                        "EmptyConfiguration.json");

        var bobTheBuilder = Bob.builder();
        bobTheBuilder.name("Stand-in for EntityController");
        bob = bobTheBuilder.build();
        bob.setForce(Force.BLUE);

        FeatureSpecification fs = FeatureSpecification.builder().featureClass(Feature.class)
                .featureName(ManeuverEntityAgent.FEATURE_NAME).build();
        Resolver resolver = Resolver.makeTransientResolver();
        agent = (ManeuverEntityAgent) resolver.loadAndResolveDependencies(fs);
        assertNotNull(agent);

        blackboard = resolver.getServicesFromCache(Blackboard.class).get(0);
        assertNotNull(blackboard);

        blackboard.addEntry("SYSTEM:EntityController", bob.getHandle(), Blackboard.BIGBANG);
        logger.info("Done setup");

        Injector.inject(agent, bob.getHandle());
        Injector.inject(blackboard, bob.getHandle());
    }

    @Test
    void tick() {
        Position p0 = Position.of(0.0, 0.0);
        Heading h0 = Heading.of(90.0);
        Speed s0 = Speed.of(2.0);

        ManeuverState ms = ManeuverState.of(bob.getHandle(), 0, p0, h0, s0);

        agent.process(ms, 0, 10000,
                Address.makeExternalAddress(bob.getHandle()), 1,
                Address.makeExternalAddress(bob.getHandle()));

        ProcessingResult output = agent.tick(20000);

        assertEquals(1, output.messages.size());

        Message sm = output.messages.get(0);

        assertTrue(sm instanceof EntityEvent);
        EntityEvent entityEvent = (EntityEvent) sm;

        Object contents = entityEvent.getContent();
        assertNotNull(contents);
        assertTrue(contents instanceof ManeuverState);
        ManeuverState got = (ManeuverState) contents;

        ManeuverState expected = ManeuverState.of(bob.getHandle(),
                20000, Position.of(40.0, 0.0),
                h0, s0);

        assertEquals(expected, got);

        output = agent.tick(40000);

        assertEquals(1, output.messages.size());
        sm = output.messages.get(0);

        assertTrue(sm instanceof EntityEvent);
        entityEvent = (EntityEvent) sm;

        contents = entityEvent.getContent();
        assertNotNull(contents);
        assertTrue(contents instanceof ManeuverState);
        got = (ManeuverState) contents;

        expected = ManeuverState.of(bob.getHandle(),
                40000, Position.of(80.0, 0.0),
                h0, s0);

        assertEquals(expected, got);
    }


    @Test
    void contentHandled() {
        assertNotNull(agent);
        assertNotNull(blackboard);

        List<Class<? extends HandlerContent>> out = agent.contentHandled();
        assertTrue(out.contains(ManeuverState.class));
        assertTrue(out.contains(ManeuverStateQuery.class));
        assertTrue(out.contains(Position.class));
        assertTrue(out.contains(Heading.class));
        assertTrue(out.contains(Speed.class));
        assertFalse(out.contains(Integer.class));
    }

    @Test
    void process() {
        Position p0 = Position.of(0.0, 0.0);
        Position p1 = Position.of(10.0, 10.0);
        Heading h0 = Heading.of(0.0);
        Heading h1 = Heading.of(90.0);
        Speed s0 = Speed.of(0.0);
        Speed s1 = Speed.of(2.0);

        ManeuverState expected = ManeuverState.of(bob.getHandle(), 0, p0, h0, s0);
        ProcessingResult processingResult = agent.process(ManeuverStateQuery.builder().build(), 0, 0,
                Address.makeExternalAddress(bob.getHandle()), 1,
                Address.makeExternalAddress(bob.getHandle()));
        assertEquals(1, processingResult.messages.size());
        Message message = processingResult.messages.get(0);
        assertTrue(message.getContent() instanceof ManeuverState);
        ManeuverState ms = (ManeuverState) message.getContent();
        assertEquals(expected, ms);

        expected = ManeuverState.of(bob.getHandle(), 10000, p1, h0, s0);
        processingResult = agent.process(expected, 10000, 10000,
                Address.makeExternalAddress(bob.getHandle()), 1,
                Address.makeExternalAddress(bob.getHandle()));
        assertEquals(1, processingResult.messages.size());
        message = processingResult.messages.get(0);
        assertTrue(message.getContent() instanceof ManeuverState);
        ms = (ManeuverState) message.getContent();
        assertEquals(expected, ms);

        expected = ManeuverState.of(bob.getHandle(), 20000, p1, h1, s0);
        processingResult = agent.process(expected, 20000, 20000,
                Address.makeExternalAddress(bob.getHandle()), 1,
                Address.makeExternalAddress(bob.getHandle()));
        assertEquals(1, processingResult.messages.size());
        message = processingResult.messages.get(0);
        assertTrue(message.getContent() instanceof ManeuverState);
        ms = (ManeuverState) message.getContent();
        assertEquals(expected, ms);


        expected = ManeuverState.of(bob.getHandle(), 30000, p1, h1, s1);
        processingResult = agent.process(expected, 30000, 30000,
                Address.makeExternalAddress(bob.getHandle()), 1,
                Address.makeExternalAddress(bob.getHandle()));
        assertEquals(1, processingResult.messages.size());
        message = processingResult.messages.get(0);
        assertTrue(message.getContent() instanceof ManeuverState);
        ms = (ManeuverState) message.getContent();
        assertEquals(expected, ms);

        expected = ManeuverState.of(bob.getHandle(), 40000, p0, h0, s0);
        processingResult = agent.process(expected, 40000, 40000,
                Address.makeExternalAddress(bob.getHandle()), 1,
                Address.makeExternalAddress(bob.getHandle()));
        assertEquals(1, processingResult.messages.size());
        message = processingResult.messages.get(0);
        assertTrue(message.getContent() instanceof ManeuverState);
        ms = (ManeuverState) message.getContent();
        assertEquals(expected, ms);
    }

    @SuperBuilder
    static class Bob extends BaseEntity {

        @Override
        public String getPath() {
            return "SYSTEM:Bob!";
        }

    }
}
