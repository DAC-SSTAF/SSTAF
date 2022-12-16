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

package mil.sstaf.telemetry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.state.LabeledState;
import mil.sstaf.core.state.StateProperty;
import mil.sstaftest.util.BaseAgentTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class TelemetryAgentTest extends BaseAgentTest<TelemetryAgent> {

    public static final String TELEMETRY_DIR = "build" + File.separator + "tmp" + File.separator + "telemetryDir";

    static {
        preloadedClasses = List.of("mil.sstaf.blackboard.inmem.InMemBlackboard");
        TelemetryConfiguration tc = TelemetryConfiguration.builder()
                .stateKeys(List.of("Chuck", "Sarah")).build();
        setDefaultConfiguration("Telemetry Agent", tc);
        System.setProperty(TelemetryAgent.PROP_TELEMETRY_OUTPUT_DIR, TELEMETRY_DIR);
    }

    public TelemetryAgent buildFeature() {
        return new TelemetryAgent();
    }

    @BeforeEach
    void setup() {
        feature = setupFeature();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @Jacksonized
    static class ChuckState extends LabeledState {
        final double x;
        final boolean y;
        final int z;

        @StateProperty(headerLabel = "EX")
        public double getX() {
            return x;
        }

        @StateProperty(headerLabel = "Why?")
        public boolean isY() {
            return y;
        }

        @StateProperty(headerLabel = "ZEE!")
        public int getZ() {
            return z;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @Jacksonized
    static class SarahState extends LabeledState {
        final double a;
        final boolean b;

        @StateProperty(headerLabel = "Alpha")
        public double getA() {
            return a;
        }

        @StateProperty(headerLabel = "Bravo")
        public boolean isB() {
            return b;
        }
    }

    @Nested
    @DisplayName("Check the specific requirements for 'TelemetryAgent'")
    public class TelemetryAgentTests {
        @Test
        @DisplayName("Confirm that the TelemetryAgent can write CSV files in the specified directory")
        void simpleTest() {
            assertNotNull(feature);
            assertDoesNotThrow(feature::init);
            Random random = new Random();
            for (int i = 0; i < 100.0; ++i) {
                ChuckState chuck = ChuckState.builder().x(random.nextDouble()).y(random.nextBoolean())
                        .z(random.nextInt()).build();
                SarahState sarah = SarahState.builder().a(random.nextDouble()).b(random.nextBoolean()).build();
                feature.getBlackboard().addEntry("Chuck", chuck, 0);
                feature.getBlackboard().addEntry("Sarah", sarah, 0);
                feature.tick(i);
            }

            String chuckPath = TELEMETRY_DIR + File.separator + "Dummy" + File.separator + "Chuck.csv";
            File chuckFile = new File(chuckPath);
            assertTrue(chuckFile.exists());
            assertTrue(chuckFile.isFile());

            String sarahPath = TELEMETRY_DIR + File.separator + "Dummy" + File.separator + "Sarah.csv";
            File sarahFile = new File(sarahPath);
            assertTrue(sarahFile.exists());
            assertTrue(sarahFile.isFile());
        }
    }
}


