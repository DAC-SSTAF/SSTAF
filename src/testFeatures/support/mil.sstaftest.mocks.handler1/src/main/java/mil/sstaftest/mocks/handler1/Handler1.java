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

package mil.sstaftest.mocks.handler1;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.*;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaftest.mocks.api.Command1;
import mil.sstaftest.mocks.api.PinkyProvider;

import java.util.List;

public class Handler1 extends BaseHandler {

    @Requires(name = "Pinky", minorVersion = 1)
    public PinkyProvider pinky = null;
    int x = 3;
    String y = "xxxxx";
    Object q = new Object();

    public Handler1() {
        super("Handler1", 3, 1, 7, false, "Handle it Roy. Handle it, handle it");
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(Command1.class, StringContent.class);
    }

    @Override
    public String toString() {
        return "Handler1{" +
                "x=" + x +
                ", y='" + y + '\'' +
                ", q=" + q +
                ", pinky=" + pinky +
                '}';
    }

    @Override
    public void init() {
        super.init();
        if (pinky == null) {
            throw new SSTAFException("Narf!");
        }

    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms,
                                    Address from, long id, Address respondTo) {
        if (arg instanceof Command1) {
            Command1 tc1 = (Command1) arg;
            tc1.string = "Got it";
            tc1.done = true;
            Message m = this.buildNormalResponse(tc1, id, respondTo);
            return ProcessingResult.of(m);
        } else {
            return ProcessingResult.empty();
        }
    }

}

