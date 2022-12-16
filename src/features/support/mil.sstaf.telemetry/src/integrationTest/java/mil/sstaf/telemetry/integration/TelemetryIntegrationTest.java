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

package mil.sstaf.telemetry.integration;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.features.Agent;
import mil.sstaf.core.features.FeatureSpecification;
import mil.sstaf.telemetry.TelemetryAgent;
import mil.sstaf.telemetry.TelemetryConfiguration;
import mil.sstaftest.util.BaseFeatureIntegrationTest;

public class TelemetryIntegrationTest extends BaseFeatureIntegrationTest<Agent, TelemetryConfiguration> {
    /**
     * Constructor
     */
    protected TelemetryIntegrationTest() {
        super(Agent.class, getSpec());
    }

    private static FeatureSpecification getSpec() {
        return FeatureSpecification.builder()
                .featureClass(TelemetryAgent.class)
                .featureName("Telemetry Agent")
                .majorVersion(1)
                .minorVersion(0)
                .requireExact(true)
                .build();
    }
}

