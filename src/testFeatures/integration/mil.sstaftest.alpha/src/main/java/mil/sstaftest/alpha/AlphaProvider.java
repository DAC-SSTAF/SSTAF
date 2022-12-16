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

package mil.sstaftest.alpha;

import mil.sstaf.core.features.BaseFeature;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.util.SSTAFException;


public class AlphaProvider extends BaseFeature {

    @SuppressWarnings("unused")
    private FeatureConfiguration configuration;

    public AlphaProvider() {
        super("Alpha", 5, 3, 0, true, "");

    }

    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
        this.configuration = configuration;
    }

    @Override
    public void init() throws SSTAFException {
        super.init();
        if (configuration == null) {
            throw new SSTAFException("Null configuration");
        }
    }

}

