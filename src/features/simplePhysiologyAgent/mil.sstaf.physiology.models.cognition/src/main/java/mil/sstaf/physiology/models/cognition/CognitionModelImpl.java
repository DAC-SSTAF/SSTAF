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

package mil.sstaf.physiology.models.cognition;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.Requires;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaf.physiology.models.api.CognitionMetrics;
import mil.sstaf.physiology.models.api.CognitionModel;

public class CognitionModelImpl extends BaseFeature implements CognitionModel {
    public static final String FEATURE_NAME = "Cognition Model";
    public static final String BK_COGNITION_METRICS = "Cognition Metrics";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    @Requires
    Blackboard blackboard;

    public CognitionModelImpl() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "Brains!");
    }

    @Override
    public CognitionMetrics evaluate(long time_ms) {
        long maxAwake = 36 * 3600 * 1000;
        double smarts = (time_ms <= maxAwake) ? 1.0 - (double) time_ms / maxAwake : 0.0;

        var cm = CognitionMetrics.builder().smarts(smarts).build();
        blackboard.addEntry(BK_COGNITION_METRICS, cm, time_ms);
        return cm;
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
    }
}

