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
import mil.sstaftest.mocks.api.BrainProvider;
import mil.sstaftest.mocks.brain.Brain;

module mil.sstaftest.mocks.brain {
    exports mil.sstaftest.mocks.brain; // only because the test case digs into it.

    requires mil.sstaf.core;
    requires mil.sstaftest.mocks.api;
    

    provides BrainProvider with Brain;
    provides Feature with Brain;

    opens mil.sstaftest.mocks.brain to mil.sstaf.core;
}
