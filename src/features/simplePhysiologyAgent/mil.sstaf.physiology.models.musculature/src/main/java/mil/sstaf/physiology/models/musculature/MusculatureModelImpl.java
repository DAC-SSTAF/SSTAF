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

package mil.sstaf.physiology.models.musculature;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.Requires;
import mil.sstaf.core.util.PhysicalConstants;
import mil.sstaf.core.util.SSTAFException;
import mil.devcom_sc.ansur.api.ANSURIIAnthropometry;
import mil.devcom_sc.ansur.messages.Handedness;
import mil.devcom_dac.equipment.api.Item;
import mil.sstaf.physiology.models.api.MusculatureMetrics;
import mil.sstaf.physiology.models.api.MusculatureModel;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Objects;

public class MusculatureModelImpl extends BaseFeature implements MusculatureModel {

    public static final String BK_MUSCLE_METRICS = "Muscle Metrics";

    public static final String FEATURE_NAME = "Muscle Model";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    public static final double DOMINANT_STRENGTH_FACTOR = 0.85;
    public static final double NON_DOMINANT_STRENGTH_FACTOR = 0.72;

    private static final Logger logger = LoggerFactory.getLogger(MusculatureModelImpl.class);

    @Requires
    Blackboard blackboard;

    @Requires()
    ANSURIIAnthropometry anthropometry;

    ArmModel rightArm = new ArmModel();
    ArmModel leftArm = new ArmModel();
    ArmModel dominantArm;
    ArmModel nonDominantArm;

    public MusculatureModelImpl() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION, false, "Arms! Leg! Glutes!");
    }

    /**
     * Initializes this {@code Provider}
     *
     * @throws SSTAFException if an error occurs.
     */
    @Override
    public void init() throws SSTAFException {
        super.init();
        Objects.requireNonNull(anthropometry, "Anthropometry is null");
        Objects.requireNonNull(blackboard, "Blackboard is null");

        if (anthropometry.getHandedness() == Handedness.RIGHT) {
            dominantArm = rightArm;
            nonDominantArm = leftArm;
        } else {
            dominantArm = leftArm;
            nonDominantArm = rightArm;
        }
        dominantArm.init(true);
        nonDominantArm.init(false);
    }

    /**
     * Configures the {@code MusculatureModel} to get the current arm loading from the weight of
     * an {@code Item}. This enables arm strength to be modified by a changeable load such as a gun.
     *
     * @param simTime_ms             the simulation time at which the {@code Item} is added to the arms
     * @param item                   the {@code Item} that is being carried
     * @param dominantArmFraction    the fraction of the {@code Item} weight carried by the dominant arm
     * @param nonDominantArmFraction the fraction of the {@code Item} weight carried by the non-dominant arm
     */
    @Override
    public void setArmItem(long simTime_ms, Item item, double dominantArmFraction, double nonDominantArmFraction) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}:{} - setting arms item as {}(d={}/nd={})",
                    ownerHandle.getPath(), featureName, item.getName(), dominantArmFraction, nonDominantArmFraction);
        }
        dominantArm.setItem(simTime_ms, item, dominantArmFraction);
        nonDominantArm.setItem(simTime_ms, item, nonDominantArmFraction);
    }

    @Override
    public void setArmLoads_N(long simTime_ms, double leftArm_N, double rightArm_N) {
        calculateArmStrength(simTime_ms); // update current strength;
        leftArm.setLoad(simTime_ms, leftArm_N);
        rightArm.setLoad(simTime_ms, rightArm_N);
    }

    /**
     * Sets the configuration for this provider.
     * <p>
     * The configuration is applied when {@code init()} is invoked.
     * @param configuration
     *
     */
    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
    }

    void calculateArmStrength(long time_ms) {
        rightArm.calculateCurrentStrength(time_ms);
        leftArm.calculateCurrentStrength(time_ms);
    }

    @Override
    public MusculatureMetrics evaluate(long time_ms) {
        calculateArmStrength(time_ms);

        var builder = MusculatureMetrics.builder();
        builder.handedness(anthropometry.getHandedness());
        builder.rightArmMaxStrength_N(rightArm.getMaximumStrength_N());
        builder.righArmMaxImpulse_Ns(rightArm.getMaxImpulse_Ns());
        builder.rightArmLoad_N(rightArm.getLoad());
        builder.leftArmMaxStrength_N(leftArm.getMaximumStrength_N());
        builder.leftArmMaxImpulse_Ns(leftArm.getMaxImpulse_Ns());
        builder.leftArmLoad_N(leftArm.getLoad());

        builder.leftArmStrength_N(leftArm.getCurrentStrength_N(time_ms));
        builder.leftArmFatigued(leftArm.isFatigued(time_ms));
        builder.leftRemainingImpulse_Ns(leftArm.getRemainingImpulse_Ns(time_ms));

        builder.rightArmStrength_N(rightArm.getCurrentStrength_N(time_ms));
        builder.rightArmFatigued(rightArm.isFatigued(time_ms));
        builder.rightRemainingImpulse_Ns(rightArm.getRemainingImpulse_Ns(time_ms));

        MusculatureMetrics metrics = builder.build();
        blackboard.addEntry(BK_MUSCLE_METRICS, metrics, time_ms, time_ms + 20 * 60 * 1000);
        return metrics;
    }

    class ArmModel {
        public static final int STRENGTH_SCALE = 100;
        private double maximumStrength_N;
        private double maxImpulse_Ns;

        private double remainingImpulse_Ns;
        private double currentStrength_N;

        private boolean fatigued = false;

        private long updateTime_ms;
        private double baseLoad_N;
        private Item item = null;
        private double weightFraction = 0.0;

        void init(boolean isDominant) {
            //
            // Fake strength until later.
            //
            double weight = anthropometry.getWeight_kg() * PhysicalConstants.GRAVITY;
            double chestAdjustment = 1.0 + 4 * (anthropometry.getSpan_cm() / anthropometry.getHeight_cm() - 1.0);
            double dominantAdjustment = isDominant ? DOMINANT_STRENGTH_FACTOR : NON_DOMINANT_STRENGTH_FACTOR;
            double max = dominantAdjustment * chestAdjustment * weight;

            maximumStrength_N = max;
            currentStrength_N = max;
            maxImpulse_Ns = maximumStrength_N * STRENGTH_SCALE;
            remainingImpulse_Ns = maxImpulse_Ns;
            baseLoad_N = 0.0;
            updateTime_ms = 0;
        }

        void setItem(long time_ms, Item item, double weightFraction) {
            calculateCurrentStrength(time_ms);
            this.updateTime_ms = time_ms;
            this.item = item;
            this.weightFraction = weightFraction;
        }

        void setLoad(long time_ms, double load_N) {
            calculateCurrentStrength(time_ms);
            this.updateTime_ms = time_ms;
            this.baseLoad_N = load_N;
        }

        double getLoad() {
            double itemLoad = item == null ? 0.0 :
                    item.getMass_kg() * PhysicalConstants.GRAVITY * weightFraction;
            return itemLoad + baseLoad_N;
        }

        void calculateCurrentStrength(long time_ms) {
            if (time_ms > updateTime_ms) {
                long deltaT_ms = time_ms - updateTime_ms;
                double deltaT_s = deltaT_ms / 1000.0;
                double currentLoad_N = getLoad();
                if (currentLoad_N > 0) {
                    remainingImpulse_Ns = remainingImpulse_Ns - deltaT_s * currentLoad_N;
                    if (remainingImpulse_Ns <= 0) remainingImpulse_Ns = 0;
                } else {
                    remainingImpulse_Ns += deltaT_s * maximumStrength_N / (1.2 * STRENGTH_SCALE);
                    if (remainingImpulse_Ns >= maxImpulse_Ns) remainingImpulse_Ns = maxImpulse_Ns;
                }
                currentStrength_N = remainingImpulse_Ns / STRENGTH_SCALE;
                fatigued = currentStrength_N <= currentLoad_N;
                updateTime_ms = time_ms;
            }
        }

        public double getMaximumStrength_N() {
            return maximumStrength_N;
        }

        public double getMaxImpulse_Ns() {
            return maxImpulse_Ns;
        }

        public double getRemainingImpulse_Ns(long time_ms) {
            calculateCurrentStrength(time_ms);
            return remainingImpulse_Ns;
        }

        public double getCurrentStrength_N(long time_ms) {
            calculateCurrentStrength(time_ms);
            return currentStrength_N;
        }

        public boolean isFatigued(long time_ms) {
            calculateCurrentStrength(time_ms);
            return fatigued;
        }

        public long getUpdateTime_ms() {
            return updateTime_ms;
        }

        public double getBaseLoad_N() {
            return baseLoad_N;
        }

        public Item getItem() {
            return item;
        }

        public double getWeightFraction() {
            return weightFraction;
        }
    }
}

