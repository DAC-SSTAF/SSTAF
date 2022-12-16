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
import mil.sstaf.physiology.models.api.HydrationModel;
import mil.sstaf.physiology.models.hydration.HydrationModelImpl;

module mil.sstaf.physiology.models.hydration {
    exports mil.sstaf.physiology.models.hydration;

    requires mil.sstaf.core;
    requires mil.sstaf.physiology.models.api;
    
    requires org.slf4j;
    requires mil.sstaf.blackboard.api;

    provides HydrationModel with HydrationModelImpl;
    provides Feature with HydrationModelImpl;

    opens mil.sstaf.physiology.models.hydration to mil.sstaf.core;
}
