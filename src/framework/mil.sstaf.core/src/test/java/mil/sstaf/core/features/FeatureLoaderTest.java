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

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.configuration.SSTAFConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FeatureLoaderTest {

    @BeforeEach
    void setUp() {
        FeatureLoader.registerClass(BananaFeature.class);
        FeatureLoader.registerClass(HarryHandler.class);
        FeatureLoader.registerClass(AliceAgent.class);
    }

    @AfterEach
    void reset() {
        FeatureLoader.clearClassRegistry();
    }

    @Test
    void canRegisterAProvider() {
        assertEquals(3, FeatureLoader.getRegisteredClasses().size());
    }

    @Test
    void canInstantiateClasses() {
        FeatureLoader<BananaFeature> featureLoader1 = new FeatureLoader<>(BananaFeature.class, this, ModuleLayer.boot());
        Assertions.assertNotNull(featureLoader1);

        Assertions.assertDoesNotThrow(() -> {
            List<BananaFeature> candidates = featureLoader1.generateFeatureInstances();
            assertEquals(1, candidates.size());
            BananaFeature bananaFeature = candidates.get(0);
            assertEquals(1, bananaFeature.getMajorVersion());
            assertEquals(2, bananaFeature.getMinorVersion());
            assertEquals(3, bananaFeature.getPatchVersion());
        });

        FeatureLoader<HarryHandler> featureLoader2 = new FeatureLoader<>(HarryHandler.class, this, ModuleLayer.boot());
        Assertions.assertNotNull(featureLoader2);

        Assertions.assertDoesNotThrow(() -> {
            List<HarryHandler> candidates = featureLoader2.generateFeatureInstances();
            assertEquals(1, candidates.size());
            HarryHandler harryHandler = candidates.get(0);
            assertEquals(5, harryHandler.getMajorVersion());
            assertEquals(3, harryHandler.getMinorVersion());
            assertEquals(1, harryHandler.getPatchVersion());
        });

        FeatureLoader<AliceAgent> featureLoader3 = new FeatureLoader<>(AliceAgent.class, this, ModuleLayer.boot());
        Assertions.assertNotNull(featureLoader3);

        Assertions.assertDoesNotThrow(() -> {
            List<AliceAgent> candidates = featureLoader3.generateFeatureInstances();
            assertEquals(1, candidates.size());
            AliceAgent AliceAgent = candidates.get(0);
            assertEquals(1, AliceAgent.getMajorVersion());
            assertEquals(1, AliceAgent.getMinorVersion());
            assertEquals(1, AliceAgent.getPatchVersion());
        });

        FeatureLoader<Feature> featureLoader4 = new FeatureLoader<>(Feature.class, this, ModuleLayer.boot());
        Assertions.assertNotNull(featureLoader4);

        Assertions.assertDoesNotThrow(() -> {
            List<Feature> candidates = featureLoader4.generateFeatureInstances();
            assertEquals(3, candidates.size());
        });
    }

    @Test
    void meetsRequirementsWorks() {
        HarryHandler hh1 = new HarryHandler();
        HarryHandler hh2 = new HarryHandler2();
        HarryHandler hh3 = new HarryHandler3();

        assertFalse(FeatureLoader.meetsRequirements(null, "Harry", 4, 0, true));

        assertTrue(FeatureLoader.meetsRequirements(hh1, "Harry", 4, 0, false));
        assertFalse(FeatureLoader.meetsRequirements(hh1, "Harry", 4, 0, true));
        assertTrue(FeatureLoader.meetsRequirements(hh1, "Harry", 5, 3, true));

        assertTrue(FeatureLoader.meetsRequirements(hh2, "Harry", 4, 0, false));
        assertFalse(FeatureLoader.meetsRequirements(hh2, "Harry", 5, 3, true));
        assertTrue(FeatureLoader.meetsRequirements(hh2, "Harry", 5, 4, true));

        assertTrue(FeatureLoader.meetsRequirements(hh3, "Harry", 4, 0, false));
        assertFalse(FeatureLoader.meetsRequirements(hh3, "Harry", 4, 0, true));
        assertTrue(FeatureLoader.meetsRequirements(hh3, "Harry", 6, 0, true));


    }

    @Test
    void compareVersionsWorks() {
        HarryHandler harryHandler = new HarryHandler();
        HarryHandler harryHandler2 = new HarryHandler2();
        HarryHandler harryHandler3 = new HarryHandler3();

        assertEquals(0, FeatureLoader.compareVersions(harryHandler, harryHandler));
        assertEquals(0, FeatureLoader.compareVersions(harryHandler2, harryHandler2));
        assertEquals(0, FeatureLoader.compareVersions(harryHandler3, harryHandler3));

        assertEquals(-1, FeatureLoader.compareVersions(harryHandler2, harryHandler));
        assertEquals(-1, FeatureLoader.compareVersions(harryHandler3, harryHandler2));
        assertEquals(-1, FeatureLoader.compareVersions(harryHandler3, harryHandler));

        assertEquals(1, FeatureLoader.compareVersions(harryHandler, harryHandler2));
        assertEquals(1, FeatureLoader.compareVersions(harryHandler2, harryHandler3));
        assertEquals(1, FeatureLoader.compareVersions(harryHandler, harryHandler3));
    }

    @Test
    void findBestMatchWorks() {
        List<HarryHandler> candidates = List.of(new HarryHandler(),
                new HarryHandler2(), new HarryHandler3());

        HarryHandler hh = FeatureLoader.findBestMatch(candidates, null, "Harry", 4, 0, false);
        assertNotNull(hh);
        assertEquals(6, hh.getMajorVersion());
        assertEquals(0, hh.getMinorVersion());
        assertEquals(0, hh.getPatchVersion());

        HarryHandler hh2 = FeatureLoader.findBestMatch(candidates, null, "Harry", 5, 4, true);
        assertNotNull(hh2);
        assertEquals(HarryHandler2.class, hh2.getClass());

    }

    @Test
    void loadWorks() {
        FeatureLoader.registerClass(HarryHandler2.class);
        FeatureLoader.registerClass(HarryHandler3.class);

        assertEquals(5, FeatureLoader.getRegisteredClasses().size());

        FeatureLoader<HarryHandler> featureLoader = new FeatureLoader<>(HarryHandler.class, this, SSTAFConfiguration.getInstance().getRootLayer());

        Optional<HarryHandler> optionalHarryHandler =
                featureLoader.load("Harry", 4, 0, false);
        assertTrue(optionalHarryHandler.isPresent());
        HarryHandler hh = optionalHarryHandler.get();
        assertEquals(HarryHandler3.class, hh.getClass());
    }


    static class BananaFeature extends BaseFeature {

        public BananaFeature() {
            super("Banana", 1, 2, 3, false, "Flingin' Poo!");
        }

    }

    static class HarryHandler extends BaseHandler {

        public HarryHandler() {
            super("Harry", 5, 3, 1, false, "I handle integers with panache");
        }

        protected HarryHandler(String featureName, int majorVersion, int minorVersion, int patchVersion, boolean requiresConfiguration, String description) {
            super(featureName, majorVersion, minorVersion, patchVersion, requiresConfiguration, description);
        }

        @Override
        public List<Class<? extends HandlerContent>> contentHandled() {
            return List.of(IntContent.class);
        }

        @Override
        public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
            return null;
        }

    }

    static class HarryHandler2 extends HarryHandler {

        public HarryHandler2() {
            super("Harry", 5, 4, 1, false, "");
        }

    }

    static class HarryHandler3 extends HarryHandler {

        public HarryHandler3() {
            super("Harry", 6, 0, 0, false, "");
        }
    }

    static class AliceAgent extends BaseAgent {

        public AliceAgent() {
            super("Alice", 1, 1, 1, false,
                    "You can get anything you want.");
        }

        @Override
        public ProcessingResult tick(long currentTime_ms) {
            return ProcessingResult.empty();
        }

        @Override
        public List<Class<? extends HandlerContent>> contentHandled() {
            return List.of();
        }

        @Override
        public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
            return null;
        }

    }
}

