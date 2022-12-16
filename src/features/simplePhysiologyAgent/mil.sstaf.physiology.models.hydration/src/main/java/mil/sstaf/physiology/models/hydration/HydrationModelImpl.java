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

package mil.sstaf.physiology.models.hydration;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.Requires;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaf.physiology.models.api.HydrationMetrics;
import mil.sstaf.physiology.models.api.HydrationModel;

public class HydrationModelImpl extends BaseFeature implements HydrationModel {
    public static final String FEATURE_NAME = "Hydration Model";
    public static final String BK_HYDRATION_METRICS = "Hydration Metrics";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    @Requires
    Blackboard blackboard;

    public HydrationModelImpl() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "A model for hydration");
    }

    @Override
    public HydrationMetrics evaluate(long time_ms) {
        long maxTime = 72 * 3600 * 1000;
        double hyd = (time_ms <= maxTime) ? 1.0 - (double) time_ms / maxTime : 0.0;
        var hm = HydrationMetrics.builder().hydrationLevel(hyd).timestamp_ms(time_ms).build();
        blackboard.addEntry(BK_HYDRATION_METRICS, hm, time_ms);
        return hm;
    }

    /**
     * Initializes this {@code Provider}
     *
     * @throws SSTAFException if an error occurs.
     */
    @Override
    public void init() throws SSTAFException {

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

    }
}

