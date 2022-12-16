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

package mil.sstaf.physiology.models.api;

import mil.devcom_dac.equipment.api.Item;

/**
 * Muscle model
 */
public interface MusculatureModel extends PhysiologyModel<MusculatureMetrics> {

    /**
     * Sets a static load on each arm
     *
     * @param simTime_ms the time at which the load is applied
     * @param leffArm_N  the load on the left arm
     * @param rightArm_N i
     */
    void setArmLoads_N(long simTime_ms, double leffArm_N, double rightArm_N);

    /**
     * Configures the {@code MusculatureModel} to get the current arm loading from the weight of
     * an {@code Item}. This enables arm strength to be modified by a changeable load such as a gun.
     *
     * @param simTime_ms             the simulation time at which the {@code Item} is added to the arms
     * @param item                   the {@code Item} that is being carried
     * @param dominantArmFraction    the fraction of the {@code Item} weight carried by the dominant arm
     * @param nondominantArmFraction the fraction of the {@code Item} weight carried by the non-dominant arm
     */
    void setArmItem(long simTime_ms, Item item, double dominantArmFraction, double nondominantArmFraction);

}

