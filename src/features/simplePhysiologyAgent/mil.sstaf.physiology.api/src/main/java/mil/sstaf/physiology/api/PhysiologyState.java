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

package mil.sstaf.physiology.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.state.LabeledState;
import mil.sstaf.core.state.StateProperty;
import mil.sstaf.physiology.models.api.*;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
public class PhysiologyState extends LabeledState {

    public final CognitionMetrics cognitionMetrics;
    public final CardiovascularMetrics cardiovascularMetrics;
    public final MusculatureMetrics musculatureMetrics;
    public final EnergyMetrics energyMetrics;
    public final HydrationMetrics hydrationMetrics;
    public final RespirationMetrics respirationMetrics;
    public final VisionMetrics visionMetrics;
    public final Boolean allowNullMembers;

    @StateProperty(headerLabel = "Cognition")
    public CognitionMetrics getCognitionMetrics() {
        return cognitionMetrics;
    }

    @StateProperty(headerLabel = "Cardiovascular")
    public CardiovascularMetrics getCardiovascularMetrics() {
        return cardiovascularMetrics;
    }

    @StateProperty(headerLabel = "Musculature")
    public MusculatureMetrics getMusculatureMetrics() {
        return musculatureMetrics;
    }

    @StateProperty(headerLabel = "Energy")
    public EnergyMetrics getEnergyMetrics() {
        return energyMetrics;
    }

    @StateProperty(headerLabel = "Hydration")
    public HydrationMetrics getHydrationMetrics() {
        return hydrationMetrics;
    }

    @StateProperty(headerLabel = "Respiration")
    public RespirationMetrics getRespirationMetrics() {
        return respirationMetrics;
    }

    @StateProperty(headerLabel = "Vision")
    public VisionMetrics getVisionMetrics() {
        return visionMetrics;
    }
}

