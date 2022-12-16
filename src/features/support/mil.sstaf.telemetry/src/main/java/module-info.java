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

import mil.sstaf.core.features.Agent;
import mil.sstaf.core.features.Feature;
import mil.sstaf.core.features.Handler;
import mil.sstaf.telemetry.TelemetryAgent;

module mil.sstaf.telemetry {
    exports mil.sstaf.telemetry;

    requires mil.sstaf.core;
    requires org.slf4j;
    
    requires mil.sstaf.blackboard.api;

    provides Feature with TelemetryAgent;
    provides Handler with TelemetryAgent;
    provides Agent with TelemetryAgent;

    opens mil.sstaf.telemetry to mil.sstaf.core, com.fasterxml.jackson.databind;
}
