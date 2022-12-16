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

import mil.sstaf.core.features.Feature;
import mil.sstaftest.anthropometry.api.Anthropometry;
import mil.sstaftest.anthropometry.simple.AnthroConfig;

module mil.sstaftest.anthropometry.simple {
    exports mil.sstaftest.anthropometry.simple;

    requires mil.sstaf.core;
    requires mil.sstaftest.anthropometry.api;
    

    provides Feature with AnthroConfig;
    provides Anthropometry with AnthroConfig;

    opens mil.sstaftest.anthropometry.simple to mil.sstaf.core;
}
