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

import java.util.List;
import java.util.Objects;

public class Handler3 extends BaseHandler {



    @Requires(name = "Handler2")
    Handler2 handler2;
    @Requires(name = "Handler1")
    Handler1 handler1;

    public Handler3() {
        super("Handler3", 0, 1, 0, false,
                "Three shall be the number of the Handlers");
    }

    @Override
    public Class<? extends FeatureConfiguration> getConfigurationClass() {
        return FeatureConfiguration.class;
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(BlobContent.class);
    }

    @Override
    public void init() {
        super.init();
        Objects.requireNonNull(handler1);
        Objects.requireNonNull(handler2);
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        return null;
    }

}

