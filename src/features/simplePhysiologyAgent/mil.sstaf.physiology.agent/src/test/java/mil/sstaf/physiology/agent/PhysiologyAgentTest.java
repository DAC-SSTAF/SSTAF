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

package mil.sstaf.physiology.agent;

import mil.devcom_sc.ansur.messages.Handedness;
import mil.devcom_sc.ansur.messages.Sex;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.Loaders;
import mil.sstaf.core.features.Resolver;
import mil.sstaf.core.util.Injector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PhysiologyAgentTest {
//    private PhysiologyAgent agent;
//
//    @BeforeEach
//    void setUp() {
//        try {
//            System.setProperty("sstaf.preloadFeatureClasses", "true");
//            Loaders.preloadFeatureClasses(
//                    "mil.sstaf.blackboard.inmem.InMemBlackboard",
//                    "mil.devcom_sc.ansur.handler.ANSURIIHandler",
//                    "mil.sstaf.physiology.models.cognition.CognitionModelImpl",
//                    "mil.sstaf.physiology.models.cardiovascular.CardioModelImpl",
//                    "mil.sstaf.physiology.models.respiration.RespirationModelImpl",
//                    "mil.sstaf.physiology.models.hydration.HydrationModelImpl",
//                    "mil.sstaf.physiology.models.musculature.MusculatureModelImpl",
//                    "mil.sstaf.physiology.models.energy.EnergyModelImpl",
//                    "mil.sstaf.physiology.models.vision.VisionModelImpl"
//            );
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            fail();
//        }
//
//        agent = new PhysiologyAgent();
//        EntityHandle owner = EntityHandle.makeDummyHandle();
//        Injector.inject(agent, owner);
//        JSONObject config = new JSONObject();
//        JSONObject aconfig = new JSONObject();
//        aconfig.put(ValueKey.AGE.name(), 25);
//        aconfig.put(ValueKey.WRITING_PREFERENCE.name(), Handedness.RIGHT.name());
//        aconfig.put(ValueKey.STATURE.name(), 175.0);
//        aconfig.put(ValueKey.WEIGHT_KG.name(), 100.0);
//        aconfig.put(ValueKey.GENDER.name(), Sex.MALE.name());
//        aconfig.put(ValueKey.SPAN.name(), 175.00);
//        config.put("Simple Anthropometry", aconfig);
//        Resolver resolver = Resolver.makeTransientResolver(config);
//        resolver.resolveDependencies(agent);
//    }
//
//    @Test
//    void simpleTest() {
//        assertNotNull(agent);
//        assertDoesNotThrow(agent::init);
//    }
}

