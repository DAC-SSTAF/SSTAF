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
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.*;
import mil.sstaf.core.util.Injected;
import mil.sstaf.core.util.SSTAFException;

import java.util.List;

public class Handler1 extends BaseHandler {

    @Injected
    public String myString;

    @Injected(name = "+")
    public Long posLong;

    @Injected(name = "-")
    public Long negLong;

    @Requires(name = "Pinky")
    public PinkyProvider pinky = null;

    public Handler1() {
        super("Handler1", 3, 1, 7, false,
                "Handle it Roy. Handle it, handle it");
        myString = null;
        posLong = null;
        negLong = null;
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(Command1.class, StringContent.class);
    }

    @Override
    public void init() {
        super.init();
        if (pinky == null) {
            throw new SSTAFException("Narf!");
        }

    }

    @Override
    public Class<? extends FeatureConfiguration> getConfigurationClass() {
        return FeatureConfiguration.class;
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        if (arg instanceof Command1) {
            Command1 tc1 =  Command1.builder().done(true).string("Got it").build();
            Message result = this.buildNormalResponse(tc1, id, respondTo);
            return ProcessingResult.of(result);
        } else {
            return ProcessingResult.empty();
        }
    }
}

