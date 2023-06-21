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

import mil.devcom_dac.equipment.api.EquipmentManagement;
import mil.devcom_dac.equipment.api.SoldierKit;
import mil.devcom_dac.equipment.handler.EquipmentHandler;
import mil.sstaf.core.features.Feature;

module mil.devcom_dac.equipment.handler {
    exports mil.devcom_dac.equipment.handler;

    requires mil.sstaf.core;
    requires mil.devcom_dac.equipment.messages;
    requires mil.devcom_dac.equipment.api;

    requires org.slf4j;
    requires mil.sstaf.blackboard.api;

    provides EquipmentManagement with EquipmentHandler;
    provides SoldierKit with EquipmentHandler;
    provides Feature with EquipmentHandler;

    opens mil.devcom_dac.equipment.handler to mil.sstaf.core;
}
