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

import mil.devcom_sc.ansur.api.ANSURIIAnthropometry;
import mil.devcom_sc.ansur.handler.ANSURIIHandler;
import mil.sstaf.core.features.Feature;
import mil.sstaf.core.features.Handler;

module mil.devcom_sc.ansur.handler {
    exports mil.devcom_sc.ansur.handler;

    requires mil.sstaf.core;
    
    requires mil.devcom_sc.ansur.api;
    requires commons.csv;

    provides Feature with ANSURIIHandler;
    provides Handler with ANSURIIHandler;
    provides ANSURIIAnthropometry with ANSURIIHandler;

    opens mil.devcom_sc.ansur.handler to mil.sstaf.core;
}

