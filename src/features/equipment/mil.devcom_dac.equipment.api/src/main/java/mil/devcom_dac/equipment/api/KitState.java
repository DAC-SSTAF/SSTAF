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

package mil.devcom_dac.equipment.api;

import lombok.Builder;
import mil.sstaf.core.state.StateProperty;

/**
 * State object for reporting to Telemetry
 */
@Builder
public class KitState {
    public static final String BK_KEY = "KitState";

    private final double kitMass;
    private final double gunMass;
    private final int numRoundsRemaining;

    @StateProperty(headerLabel = "kitMass")
    public double getKitMass() {
        return kitMass;
    }

    @StateProperty(headerLabel = "gunMass")
    public double getGunMass() {
        return gunMass;
    }

    @StateProperty(headerLabel = "numRoundsRemaining")
    public int getNumRoundsRemaining() {
        return numRoundsRemaining;
    }


}

