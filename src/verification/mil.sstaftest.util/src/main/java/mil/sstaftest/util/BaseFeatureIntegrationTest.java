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

package mil.sstaftest.util;

import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.*;
import mil.sstaf.core.util.Injector;
import org.apache.commons.math3.random.MersenneTwister;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for integration testing implementations of {@link Feature}.
 * <p>
 * This class implements a basic integration test for a Feature
 * <p>
 * Using this test framework requires some simple ceremony.
 * </p>
 * <ol>
 *     <li>The implementation classes must pass the {@code FeatureSpecification} for the feature to be
 *     tested in to this constructor. Typically this will look like:
 *      <pre>
 *      {@code super(FeatureSpecification.builder() ... .build()}
 *      </pre>
 *      The specification should include the interface class for the feature.
 *     </li>
 *
 * <li>
 *     The configuration for each {@code Feature} must be defined. This is accomplished in the {@code static}
 *     block using the {@link #setDefaultConfiguration(String, FeatureConfiguration)} method.
 * </li>
 * </ol>
 *
 * @param <T> The type of the {@code Feature} to be tested.
 * @author Ron Bowers
 * @version 1.0
 * @since 1.0.0
 */
@DisabledIfSystemProperty(named = "sstaf.disableBaseFeatureTests", matches = "true")
abstract public class BaseFeatureIntegrationTest<T extends Feature,
        S extends FeatureConfiguration> {

    protected static Map<String, FeatureConfiguration> defaultConfigMap = new HashMap<>();
    protected Map<FeatureSpecification, Feature> featureMap = new HashMap<>();
    protected final FeatureSpecification featureSpecification;
    protected Class<T> apiClass;
    protected T feature;
    protected Map<String, FeatureConfiguration> configurationMap;
    protected MersenneTwister randomGenerator = new MersenneTwister(12345);

    /**
     * Constructor
     */
    protected BaseFeatureIntegrationTest(Class<T> apiClass, FeatureSpecification fs) {
        this.apiClass = apiClass;
        this.featureSpecification = fs;
        this.configurationMap = defaultConfigMap;
    }

    /**
     * Sets the configuration for the {@code Feature} under test as well as any other features, handlers or agents that
     * are required by this {@code Feature}.
     *
     * @param featureName the name of the {@code Feature} to which the configuration is to be applied
     * @param cfg         the configuration object
     */
    protected static void setDefaultConfiguration(String featureName, FeatureConfiguration cfg) {
        defaultConfigMap.put(featureName, cfg);
    }


    /**
     * Provides the next random seed from the random number generator.
     *
     * @return a new random long
     */
    protected long getRandomSeed() {
        return randomGenerator.nextLong();
    }

    /**
     * @return the configuration for the {@code Feature
     */
    protected FeatureConfiguration getFeatureConfiguration(Feature f) {
        assertNotNull(f, "Feature is null");
        String name = f.getName();
        assertNotNull(name, "Feature getName() returned null");

        FeatureConfiguration featureConfig;
        if (configurationMap.containsKey(name)) {
            featureConfig = configurationMap.get(name);
        } else {
            featureConfig = FeatureConfiguration.builder().build();
        }
        return featureConfig;
    }

    /**
     * Configures the {@code Feature} under test for use given the default configurations.
     *
     * @return the {@code Feature}
     */
    public T loadAndResolveFeature() {
        Resolver resolver = new Resolver(featureMap, configurationMap,
                EntityHandle.makeDummyHandle(), getRandomSeed(), ModuleLayer.boot());
        Feature f = resolver.loadAndResolveDependencies(featureSpecification);
        if (apiClass.isAssignableFrom(f.getClass())) {
            T myFeature = apiClass.cast(f);assertNotNull(myFeature, "Feature is null");
            resolveFeature(myFeature);
            configureFeature(myFeature);
            myFeature.init();
            return myFeature;
        } else {
            String report = Loaders.generateServiceReport(Feature.class);
            fail("Could not find service!\n" + report);
            return null;
        }
    }

    /**
     * Configures the {@code Feature} using the default configuration.
     *
     * @param myFeature the {@code Feature}
     */
    protected void configureFeature(T myFeature) {
        FeatureConfiguration configuration = getFeatureConfiguration(myFeature);
        configuration.setSeed(getRandomSeed());
        myFeature.configure(configuration);
    }

    /**
     * Resolves any dependencies expressed by the {@code Feature}. The default configuration set will be used
     * to configure any newly-loaded features, handlers or agents.
     *
     * @param myFeature the {@code Feature}
     */
    protected void resolveFeature(T myFeature) {
        Resolver resolver = Resolver.makeTransientResolver(configurationMap);
        resolver.resolveDependencies(myFeature);
        EntityHandle owner = EntityHandle.makeDummyHandle();
        Injector.inject(myFeature, owner);
    }

    /**
     * Tests
     */
    @Nested
    @DisplayName("Check service loading and resolution")
    public class FeatureContractTests {
        @Test
        @DisplayName("Confirm that the module containing this feature can be loaded and transitive dependencies resolved")
        public void testLoadAndResolve() {
            T myFeature = loadAndResolveFeature();
            assertNotNull(myFeature);
            assertEquals(featureSpecification.featureName, myFeature.getName());
            assertTrue(myFeature.getMajorVersion() >= featureSpecification.majorVersion);
        }

    }

}

