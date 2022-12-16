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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.state.LabeledState;
import mil.sstaf.core.state.State;
import mil.sstaf.core.state.StateProperty;
import mil.devcom_sc.ansur.messages.Handedness;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
public class MusculatureMetrics extends LabeledState {

    private final double rightArmMaxStrength_N;
    private final double righArmMaxImpulse_Ns;
    private final double leftArmMaxStrength_N;
    private final double leftArmMaxImpulse_Ns;
    private final double rightArmStrength_N;
    private final boolean rightArmFatigued;
    private final double leftArmStrength_N;
    private final boolean leftArmFatigued;
    private final double rightArmLoad_N;
    private final double leftArmLoad_N;
    private final double rightRemainingImpulse_Ns;
    private final double leftRemainingImpulse_Ns;
    public final Handedness handedness;

    @StateProperty(headerLabel = "Right Arm Strength [N]")
    public double getRightArmStrength_N() {
        return rightArmStrength_N;
    }

    @StateProperty(headerLabel = "Right Arm Fatigued")
    public boolean isRightArmFatigued() {
        return rightArmFatigued;
    }

    @StateProperty(headerLabel = "Left Arm Strength [N]")
    public double getLeftArmStrength_N() {
        return leftArmStrength_N;
    }

    @StateProperty(headerLabel = "Left Arm Fatigued")
    public boolean isLeftArmFatigued() {
        return leftArmFatigued;
    }

    @StateProperty(headerLabel = "Non-dominant Arm Strength [N]")
    public double getNondominantArmStrength() {
        return handedness == Handedness.RIGHT ? leftArmStrength_N : rightArmStrength_N;
    }

    @StateProperty(headerLabel = "Dominant Arm Strength [N]")
    public double getDominantArmStrength() {
        return handedness == Handedness.RIGHT ? rightArmStrength_N : leftArmStrength_N;
    }

    @StateProperty(headerLabel = "Dominant Arm Max Strength [N]")
    public double getDominantArmMaxStrength() {
        return handedness == Handedness.RIGHT ? rightArmMaxStrength_N : leftArmMaxStrength_N;
    }

    @StateProperty(headerLabel = "Non-dominant Arm Max Strength [N]")
    public double getNondominantArmMaxStrength() {
        return handedness == Handedness.RIGHT ? leftArmMaxStrength_N : rightArmMaxStrength_N;
    }

    @StateProperty(headerLabel = "Right Arm Load [N]")
    public double getRightArmLoad_N() {
        return rightArmLoad_N;
    }

    @StateProperty(headerLabel = "Left Arm Load [N]")
    public double getLeftArmLoad_N() {
        return leftArmLoad_N;
    }

    @StateProperty(headerLabel = "Dominant Arm Load [N]")
    public double getDominantArmLoad() {
        return handedness == Handedness.RIGHT ? rightArmLoad_N : leftArmLoad_N;
    }

    @StateProperty(headerLabel = "Non-dominant Arm Load [N]")
    public double getNondominantArmLoad() {
        return handedness == Handedness.RIGHT ? leftArmLoad_N : rightArmLoad_N;
    }

    @StateProperty(headerLabel = "Right Arm Remaining Impulse [Ns]")
    public double getRightRemainingImpulse_Ns() {
        return rightRemainingImpulse_Ns;
    }

    @StateProperty(headerLabel = "Left Arm Remaining Impulse [Ns]")
    public double getLeftRemainingImpulse_Ns() {
        return leftRemainingImpulse_Ns;
    }
}

