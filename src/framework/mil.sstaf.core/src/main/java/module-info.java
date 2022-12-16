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

/**
 * @author Ron Bowers
 * @uses Feature
 * @uses Handler
 * @uses Agent
 * <p>
 * Base module for the Soldier and Squad Trade Space Analysis Framework (SSTAF).
 * @since 1.0
 */
module mil.sstaf.core {
    exports mil.sstaf.core.entity;
    exports mil.sstaf.core.util;
    exports mil.sstaf.core.state;
    exports mil.sstaf.core.features;
    exports mil.sstaf.core.configuration;
    exports mil.sstaf.core.module;

    requires transitive commons.math3; // For RandomGenerator
    requires transitive com.fasterxml.jackson.databind;
    requires transitive lombok;

    requires org.slf4j;

    uses Feature;
    uses Handler;
    uses Agent;

    exports mil.sstaf.core.json;

    opens mil.sstaf.core.configuration to com.fasterxml.jackson.databind;
    opens mil.sstaf.core.module to com.fasterxml.jackson.databind;
    opens mil.sstaf.core.entity to com.fasterxml.jackson.databind;
    opens mil.sstaf.core.features to com.fasterxml.jackson.databind;
    opens mil.sstaf.core.json to com.fasterxml.jackson.databind;
}

