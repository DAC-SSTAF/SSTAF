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
import mil.sstaf.core.features.Handler;
import mil.sstaftest.mocks.handler1.Handler1;

module mil.sstaftest.mocks.handlerOne {
    exports mil.sstaftest.mocks.handler1; // only because the test case digs into it.

    requires mil.sstaf.core;
    requires mil.sstaftest.mocks.api;
    

    provides Feature with Handler1;
    provides Handler with Handler1;

    opens mil.sstaftest.mocks.handler1 to mil.sstaf.core;
}
