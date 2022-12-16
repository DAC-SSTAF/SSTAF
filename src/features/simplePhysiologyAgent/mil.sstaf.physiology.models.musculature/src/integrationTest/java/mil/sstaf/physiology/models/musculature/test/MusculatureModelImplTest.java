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

package mil.sstaf.physiology.models.musculature.test;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.Loaders;
import mil.sstaf.core.features.Resolver;
import mil.sstaf.core.util.Injector;

import mil.sstaf.core.util.PhysicalConstants;
import mil.sstaf.core.configuration.SSTAFConfiguration;
import mil.sstaf.physiology.models.api.MusculatureMetrics;
import mil.sstaf.physiology.models.api.MusculatureModel;
import mil.sstaf.physiology.models.musculature.MusculatureModelImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class MusculatureModelImplTest {
//    private static final Logger logger = LoggerFactory.getLogger(MusculatureModelImplTest.class);
//
//    MusculatureModel muscleModel;
//    EntityHandle owner;
//    Address address;
//
//    @BeforeEach
//    public void setup() {
//        System.setProperty(SSTAFConfiguration.SSTAF_CONFIGURATION_PROPERTY,
//                "src" + File.separator +
//                        "integrationTest" + File.separator +
//                        "resources" + File.separator +
//                        "EmptyConfiguration.json");
//    }
//
//    void makeProvider(String configurationPath) {
//        muscleModel = Loaders.loadAsRef(MusculatureModel.class, "Muscle Model", 0, 1, false, ModuleLayer.boot());
//        JSONObject configuration =
//                JSONUtilities.loadObjectFromFile(configurationPath, null);
//        Resolver.makeTransientResolver(configuration).resolveDependencies(muscleModel);
//        owner = EntityHandle.makeDummyHandle();
//        Injector.inject(muscleModel, owner);
//        muscleModel.init();
//        address = Address.makeExternalAddress(owner);
//    }
//
//    @Test
//    void setupWorks() {
//        makeProvider("src/integrationTest/resources/testConfig1.json");
//        Assertions.assertNotNull(muscleModel);
//        MusculatureMetrics metrics = muscleModel.evaluate(0);
//        assertNotNull(metrics);
//        //
//        // Configuration
//        //
//        double d = 781.05063925;
//        double s = 661.5958356;
//        assertEquals(d, metrics.getDominantArmStrength());
//        assertEquals(s, metrics.getNondominantArmStrength());
//    }
//
//    @Test
//    void spanAdjustmentWorks() {
//        makeProvider("src/integrationTest/resources/testConfig2.json");
//        Assertions.assertNotNull(muscleModel);
//        MusculatureMetrics metrics = muscleModel.evaluate(0);
//        assertNotNull(metrics);
//        //
//        // Configuration
//        //
//
//        double d = 555.9480904805389;
//        double s = 470.9207354658683;
//        assertEquals(d, metrics.getDominantArmStrength(), 0.000001);  // Float fuzz
//        assertEquals(s, metrics.getNondominantArmStrength(), 0.000001);
//    }
//
//    @Test
//    void fatigueAndRestorationWork() {
//        makeProvider("src/integrationTest/resources/testConfig1.json");
//        double dMax = MusculatureModelImpl.DOMINANT_STRENGTH_FACTOR * 100 * PhysicalConstants.GRAVITY;
//        double sMax = MusculatureModelImpl.NON_DOMINANT_STRENGTH_FACTOR * 100 * PhysicalConstants.GRAVITY;
//        double d = dMax;
//        double s = sMax;
//        muscleModel.setArmLoads_N(0, 0.1 * s, 0.1 * d);
//
//        MusculatureMetrics metrics;
//        long time_ms = 0;
//        int fatigueLoops = 0;
//        do {
//            metrics = muscleModel.evaluate(time_ms);
//            d = metrics.getRightArmStrength_N();
//            s = metrics.getLeftArmStrength_N();
//            ++fatigueLoops;
//            time_ms += 10000.0;
//            if (logger.isTraceEnabled()) {
//                logger.trace("{}: right {}  {} left {}  {}", time_ms, d, metrics.isRightArmFatigued(),
//                        s, metrics.isLeftArmFatigued());
//            }
//        } while (d > 0 || s > 0);
//
//        muscleModel.setArmLoads_N(time_ms, 0.0, 0.0);
//        int restoreLoops = 0;
//        do {
//            metrics = muscleModel.evaluate(time_ms);
//            d = metrics.getRightArmStrength_N();
//            s = metrics.getLeftArmStrength_N();
//            ++restoreLoops;
//            time_ms += 10000;
//            if (logger.isTraceEnabled()) {
//                logger.debug("{}: right {}  {} left {}  {}", time_ms, d, metrics.isRightArmFatigued(),
//                        s, metrics.isLeftArmFatigued());
//            }
//        } while (d < dMax && s < sMax);
//
//        assertTrue(restoreLoops > fatigueLoops);
//    }
//

}

