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

package mil.sstaf.physiology.models.respiration;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.Requires;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaf.physiology.models.api.RespirationMetrics;
import mil.sstaf.physiology.models.api.RespirationModel;
import org.apache.commons.math3.random.MersenneTwister;

public class RespirationModelImpl extends BaseFeature implements RespirationModel {
    public static final String FEATURE_NAME = "Respiration Model";
    public static final String BK_RESPIRATION_METRICS = "Respiration Metrics";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    @Requires
    Blackboard blackboard;

    private final MersenneTwister rng = new MersenneTwister();

    public RespirationModelImpl() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "Just breathe!");
    }

    @Override
    public RespirationMetrics evaluate(long time_ms) {
        double sp02 = 1.0 - Math.abs(rng.nextGaussian()) * 0.5;
        var rm =  RespirationMetrics.builder()
                .spO2(sp02).timestamp_ms(time_ms).build();
        blackboard.addEntry(BK_RESPIRATION_METRICS, rm, time_ms);
        return rm;
    }

    /**
     * Initializes this {@code Provider}
     *
     * @throws SSTAFException if an error occurs.
     */
    @Override
    public void init() throws SSTAFException {
        super.init();
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
        rng.setSeed(configuration.getSeed());
    }
}

