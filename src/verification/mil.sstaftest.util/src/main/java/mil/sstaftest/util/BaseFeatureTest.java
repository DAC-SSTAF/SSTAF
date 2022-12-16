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
import mil.sstaf.core.features.Feature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.Loaders;
import mil.sstaf.core.features.Resolver;
import mil.sstaf.core.util.Injector;
import org.apache.commons.math3.random.MersenneTwister;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for testing implementations of {@link Feature}.
 * <p>
 * This class implements a framework and series of tests to confirm that an implementation of the {@code Feature}
 * interface fulfils all of the contractual obligations necessary for the implementation to be used in SSTAF.
 * At least one test class for the implementation should extend this class. If multiple test classes extends this
 * class, the base tests can be skipped by setting the {@code sstaf.disableFeatureBaseTests} system property to
 * <em>true</em> in the {@code static} block at the beginning of those test classes.
 * <p>
 * Using this test framework requires some simple ceremony.
 * </p>
 * <ol>
 * <li>
 *       The concrete test class must implement the {@link #buildFeature()} method that returns an instance
 *       of the {@code Feature} under test.
 * </li>
 *
 * <li>
 *     The feature classes that are required by the {@code Feature} under test and must be dynamically loaded
 *     must be specified. This is done in a {@code static} block with this construct:
 *     <pre>
 *     {@code preloadedClasses = List.of("class1Name", "class2Name", ...); }
 *     </pre>
 * </li>
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
abstract public class BaseFeatureTest<T extends Feature> {

    protected static List<String> preloadedClasses;
    protected static Map<String, FeatureConfiguration> defaultConfigMap = new HashMap<>();
    protected T feature;
    protected Map<String, FeatureConfiguration> configurationMap;
    protected MersenneTwister randomGenerator = new MersenneTwister(12345);

    /**
     * Constructor
     */
    protected BaseFeatureTest() {
       this.configurationMap = new HashMap<>(defaultConfigMap);
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
     * Loads the required classes
     */
    @BeforeEach
    protected void preload() {
        System.setProperty("sstaf.preloadFeatureClasses", "true");
        try {
            Loaders.preloadFeatureClasses(preloadedClasses);
        } catch (ClassNotFoundException e) {
            fail("A required class was not found during preloading.\n" + e.getMessage());
        }
    }

    /**
     * Builds and returns the {@code Feature} to be tested
     *
     * @return the {@code Feature} instance
     */
    protected abstract T buildFeature();

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
            featureConfig = configurationMap.get(f.getName());
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
    public T setupFeature() {
        T myFeature = buildFeature();
        assertNotNull(myFeature, "Feature is null");
        resolveFeature(myFeature);
        configureFeature(myFeature);
        myFeature.init();
        return myFeature;
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
    @DisabledIfSystemProperty(named = "sstaf.disableFeatureBaseTests", matches = "true")
    @DisplayName("Check requirements for implementations of 'Feature'")
    public class FeatureContractTests {

        /**
         * Confirms that the {@code Feature} under test has a no-arg constructor. A no-arg
         * constructor is required by the {@link ServiceLoader} mechanism.
         */
        @Test
        @DisplayName("Confirm that the Feature has a no-arg constructor")
        public void checkNoArg() {
            T feature = buildFeature();
            boolean foundOne = false;
            for (Constructor<?> constructor : feature.getClass().getConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    foundOne = true;
                    break;
                }
            }
            Assertions.assertTrue(foundOne, "Did not find a no-arg constructor in "
                    + feature.getClass().getName());
        }

        /**
         * Confirms that the {@link EntityHandle} associated with the owner of this {@code Feature} is retrievable
         * and correct.
         */
        @Test
        @DisplayName("Confirm that getOwner() returns the injected owner handle")
        public void getOwnerWorks() {
            T myFeature = buildFeature();

            //
            // Inline this so we have access to the dummy owner.
            //
            Resolver resolver = Resolver.makeTransientResolver(configurationMap);
            resolver.resolveDependencies(myFeature);
            EntityHandle dummy = EntityHandle.makeDummyHandle();
            Injector.inject(myFeature, dummy);

            configureFeature(myFeature);

            final String methodName = myFeature.getClass().getSimpleName() + ".getOwner()";
            EntityHandle eh = myFeature.getOwner();
            assertNotNull(eh, methodName + " returned null");
            Assertions.assertEquals(dummy, myFeature.getOwner(), methodName + " did not return the expected owner");
        }

        /**
         * Confirms tha the {@code configure()} and {@code isConfigured()} mechanisms work correctly
         */
        @Test
        @DisplayName("Confirm calling configure() works and sets isConfigured() to true")
        public void configureSetsIsConfigured() {
            T myFeature = buildFeature();
            resolveFeature(myFeature);
            final String methodName1 = myFeature.getClass().getSimpleName() + ".configure()";

            Assertions.assertDoesNotThrow(() -> configureFeature(myFeature), methodName1 + " threw");

            final String methodName2 = myFeature.getClass().getSimpleName() + ".isConfigured()";
            Assertions.assertTrue(myFeature.isConfigured(), methodName2 + " did not return 'true' as expected.");
        }

        /**
         * Confirms tha the {@code init()} and {@code isInitialized()} mechanisms work correctly
         */
        @Test
        @DisplayName("Confirm calling init() works and sets isInitialized() to true")
        public void initWorks() {
            T myFeature = buildFeature();
            resolveFeature(myFeature);
            configureFeature(myFeature);

            final String methodName1 = myFeature.getClass().getSimpleName() + ".init()";
            Assertions.assertDoesNotThrow(myFeature::init, methodName1 + " threw an exception");

            final String methodName2 = myFeature.getClass().getSimpleName() + ".isInitialized()";
            Assertions.assertTrue(myFeature.isConfigured(), methodName2 + " did not return 'true' as expected.");
        }

        /**
         * Confirms that the name, version number and description values are reasonable.
         * <p>
         * Note that the name and version number are decoupled from the class name and the artifact version number
         * generated by the build.
         * +9
         */
        @Test
        @DisplayName("Confirm name, description and version info are reasonable")
        public void checkSpecInfo() {
            T myFeature = setupFeature();

            String className = myFeature.getClass().getSimpleName();

            String name = myFeature.getName();
            String description = myFeature.getDescription();
            int majorVersion = myFeature.getMajorVersion();
            int minorVersion = myFeature.getMinorVersion();
            int patchVersion = myFeature.getPatchVersion();

            assertNotNull(name, className + ".getName() returned null. It must the 'official' user-comprehensible name.");
            assertNotNull(description, className + ".getDescription returned null. It should return something helpful");
            assertFalse(name.isEmpty(), className + ".getName() returned an empty String. It must the 'official' user-comprehensible name.");
            assertFalse(description.isEmpty(), className + ".getDescription() returned an empty String. It must the 'official' user-comprehensible name.");

            assertTrue(majorVersion >= 0, className + ".getMajorVersion() returned < 0, should be >= 0");
            assertTrue(minorVersion >= 0, className + ".getMinorVersion() returned < 0, should be >= 0");
            assertTrue(patchVersion >= 0, className + ".getPatchVersion() returned < 0, should be >= 0");

            assertFalse(majorVersion == 0 && minorVersion == 0 && patchVersion == 0,
                    "In " + className + " all version values are 0, at least one must be non-zero");

        }

    }

}

