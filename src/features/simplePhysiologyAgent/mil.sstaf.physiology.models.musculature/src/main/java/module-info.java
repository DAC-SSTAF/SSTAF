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
import mil.sstaf.physiology.models.api.MusculatureModel;
import mil.sstaf.physiology.models.musculature.MusculatureModelImpl;

module mil.sstaf.physiology.models.musculature {
    exports mil.sstaf.physiology.models.musculature;

    requires mil.sstaf.core;
    requires mil.devcom_sc.ansur.messages;
    requires mil.devcom_sc.ansur.api;
    requires mil.devcom_dac.equipment.api;

    requires mil.sstaf.blackboard.api;
    requires mil.sstaf.physiology.models.api;
    
    requires org.slf4j;

    provides MusculatureModel with MusculatureModelImpl;
    provides Feature with MusculatureModelImpl;

    opens mil.sstaf.physiology.models.musculature to mil.sstaf.core;
}
