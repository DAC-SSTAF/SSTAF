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

package mil.sstaf.core.mocks;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.features.*;
import mil.sstaf.core.util.Injected;
import mil.sstaf.core.util.SSTAFException;

import java.util.List;
import java.util.UUID;

public class Handler2 extends BaseHandler {

    @Injected
    public UUID uuid;

    @Injected
    public String string;

    @Requires(name = "Brain")
    public BrainProvider brain;

    public Handler2() {
        super("Handler2", 0, 0, 0, false,
                "This is the second handler");
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(IntContent.class, LongContent.class);
    }

    @Override
    public void init() {
        super.init();
        if (brain == null) {
            throw new SSTAFException("Zorp! Brain has not been injected.");
        }
    }

    @Override
    public Class<? extends FeatureConfiguration> getConfigurationClass() {
        return FeatureConfiguration.class;
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        return null;
    }
}

