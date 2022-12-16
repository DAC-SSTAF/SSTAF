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

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Entity;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.entity.TestEntity;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class ResolverTest {

    @Test
    void multilayerLoadWithCircularRequirementWorks() {

        FeatureLoader.registerClass(JamesBond.class);
        FeatureLoader.registerClass(AlphaProvider.class);
        FeatureLoader.registerClass(BravoProvider.class);
        FeatureLoader.registerClass(CharlieProvider.class);
        FeatureLoader.registerClass(DeltaProvider.class);
        FeatureLoader.registerClass(EchoProvider.class);

        FeatureSpecification jbSpec = FeatureSpecification.builder()
                .featureName("James Bond")
                .majorVersion(7)
                .minorVersion(0)
                .requireExact(false).build();

        ConcurrentMap<FeatureSpecification, Feature> featureCache = new ConcurrentHashMap<>();

        Map<String, FeatureConfiguration> config = new HashMap<>();

        for (String name : new String[]{"James Bond", "Alpha", "Bravo",
                "Charlie"}) {
            config.put(name, FeatureConfiguration.builder().build());
        }

        for (String name : new String[]{"Delta", "Echo"}) {
            config.put(name, ValueConfiguration.builder().myValue(17).build());
        }
        Entity testEntity = TestEntity.makeTestEntity();

        Resolver resolver = new Resolver(featureCache, config, testEntity.getHandle(), 31415, ModuleLayer.boot());

        Agent jamesBond = (Agent) resolver.loadAndResolveDependencies(jbSpec);

        Assertions.assertDoesNotThrow(jamesBond::init);
    }

    static class JamesBond extends BaseAgent {

        private FeatureConfiguration configuration;

        //@SuppressWarnings()
        @Requires(name = "Alpha", majorVersion = 5, minorVersion = 3, requireExact = true)
        private AlphaProvider alphaProvider;

        @Requires(name = "Bravo", majorVersion = 2, minorVersion = 1)
        private BravoProvider bravoProvider;

        public JamesBond() {
            super("James Bond", 7, 0, 0, true,
                    "Licensed to throw exceptions");
            ownerHandle = EntityHandle.makeDummyHandle();
        }

        @Override
        public ProcessingResult tick(long currentTime_ms) {
            return null;
        }

        @Override
        public List<Class<? extends HandlerContent>> contentHandled() {
            return List.of();
        }

        @Override
        public void init() {
            super.init();
            if (configuration == null) {
                throw new SSTAFException("Null configuration");
            }
            Objects.requireNonNull(alphaProvider, "No Alpha");
            Objects.requireNonNull(bravoProvider, "No Bravo");
            alphaProvider.init();
            bravoProvider.init();
        }

        @Override
        public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from,
                                        long id, Address respondTo) {
            return null;
        }

        @Override
        public void configure(FeatureConfiguration configuration) {
            super.configure(configuration);
            this.configuration = configuration;
        }
    }

    static class AlphaProvider extends BaseFeature {

        @SuppressWarnings("unused")
        private FeatureConfiguration configuration;

        public AlphaProvider() {
            super("Alpha", 5, 3, 0, true, "");

        }

        @Override
        public void configure(FeatureConfiguration configuration) {
            super.configure(configuration);
            this.configuration = configuration;
        }

        @Override
        public void init() throws SSTAFException {
            super.init();
            if (configuration == null) {
                throw new SSTAFException("Null configuration");
            }
        }

    }

    static class BravoProvider extends BaseFeature {

        @Requires(name = "Charlie", majorVersion = 1, minorVersion = 2, requireExact = true)
        private CharlieProvider charlieProvider;

        @Requires(name = "Delta", majorVersion = 8)
        private DeltaProvider deltaProvider;

        private FeatureConfiguration configuration;

        public BravoProvider() {
            super("Bravo", 3, 4, 0, true, "");
        }

        @Override
        public void init() throws SSTAFException {
            super.init();
            Objects.requireNonNull(charlieProvider, "No Charlie");
            Objects.requireNonNull(deltaProvider, "No Delta");
            Objects.requireNonNull(configuration, "No MapConfiguration");
            charlieProvider.init();
            deltaProvider.init();
        }

        @Override
        public void configure(FeatureConfiguration configuration) {
            super.configure(configuration);
            this.configuration = configuration;
        }
    }

    static class CharlieProvider extends BaseFeature {

        private FeatureConfiguration configuration;

        @Requires
        private EchoProvider echo;

        public CharlieProvider() {
            super("Charlie", 1, 2, 0, true, "");
        }

        @Override
        public void init() throws SSTAFException {
            super.init();
            if (configuration == null) {
                throw new SSTAFException("Null configuration");
            }
        }

        @Override
        public void configure(FeatureConfiguration configuration) {
            super.configure(configuration);
            this.configuration = configuration;
        }
    }

    @SuperBuilder
    static class ValueConfiguration extends FeatureConfiguration {
        @Getter
        int myValue;
    }

    static class DeltaProvider extends BaseFeature {

        ValueConfiguration configuration;
        int value = 4;

        public DeltaProvider() {
            super("Delta", 8, 9, 0, true, "");
        }

        @Override
        public void init() throws SSTAFException {
            super.init();
            if (configuration == null) {
                throw new SSTAFException("Null configuration");
            }
            if (value != 17) {
                throw new SSTAFException("MapConfiguration didn't work");
            }
        }

        @Override
        public void configure(FeatureConfiguration configuration) {
            super.configure(configuration);
            if (configuration instanceof ValueConfiguration) {
                this.configuration = (ValueConfiguration) configuration;
                this.value = this.configuration.getMyValue();
            }
        }
    }

    static class EchoProvider extends BaseFeature {

        ValueConfiguration configuration;
        int value = 4;

        @Requires
        BravoProvider bravo;

        public EchoProvider() {
            super("Echo", 1, 0, 0, true, "");
        }

        @Override
        public void init() throws SSTAFException {
            super.init();
            if (configuration == null) {
                throw new SSTAFException("Null configuration");
            }
            if (value != 17) {
                throw new SSTAFException("MapConfiguration didn't work");
            }
        }

        @Override
        public void configure(FeatureConfiguration configuration) {
            super.configure(configuration);
            if (configuration instanceof ValueConfiguration) {
                this.configuration = (ValueConfiguration) configuration;
                this.value = this.configuration.getMyValue();
            }
        }
    }

}

