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

package mil.sstaftest.jamesbond;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.*;
import mil.sstaf.core.util.SSTAFException;


import java.util.List;
import java.util.Objects;

public class JamesBond extends BaseAgent {

    private FeatureConfiguration configuration;

    //@SuppressWarnings()
    @Requires(name = "Alpha", majorVersion = 5, minorVersion = 3, requireExact = true)
    private Feature alphaProvider;

    @Requires(name = "Bravo", majorVersion = 2, minorVersion = 1)
    private Feature bravoProvider;

    public JamesBond() {
        super("James Bond", 7, 0, 0, true,
                "Licensed to throw exceptions");
        ownerHandle = EntityHandle.makeDummyHandle();
    }

    @Override
    public ProcessingResult tick(long currentTime_ms) {
        return null;
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of();
    }

    @Override
    public void init() {
        super.init();
        if (configuration == null) {
            throw new SSTAFException("Null configuration");
        }
        Objects.requireNonNull(alphaProvider, "No Alpha");
        Objects.requireNonNull(bravoProvider, "No Bravo");
        alphaProvider.init();
        bravoProvider.init();
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from,
                                    long id, Address respondTo) {
        return buildUnsupportedMessageResponse(arg, id, getAddress(), new Throwable());
    }

    @Override
    public void configure(FeatureConfiguration configuration) {
        super.configure(configuration);
        this.configuration =  configuration;
    }
}

