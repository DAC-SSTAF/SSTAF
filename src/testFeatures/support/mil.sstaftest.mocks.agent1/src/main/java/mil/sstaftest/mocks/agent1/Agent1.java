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

package mil.sstaftest.mocks.agent1;

import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.*;

import java.util.List;

public class Agent1 extends BaseAgent {
    public long lastTime_ms = 0;
    public int count = 0;

    public Agent1() {
        super("Agent1", 3, 1, 7, false, "");
    }

    @Override
    public ProcessingResult tick(long currentTime_ms) {
        lastTime_ms = currentTime_ms;
        count += 1;
        Message m = buildNormalResponse(IntContent.builder().value(count).build(),
                0, Address.makeExternalAddress(ownerHandle));
        return ProcessingResult.of(m);
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(LongContent.class, IntContent.class);
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms,
                                    Address from, long id, Address respondTo) {
        return ProcessingResult.empty();
    }
}
