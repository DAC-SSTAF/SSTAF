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

package mil.sstaftest.maneuver.centralagent;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.entity.*;
import mil.sstaf.core.features.*;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.util.Injected;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaftest.maneuver.api.ManeuverState;
import mil.sstaftest.maneuver.api.ManeuverStateMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManeuverCentralAgent extends BaseAgent {
    public static final String FEATURE_NAME = "Maneuver Central Agent";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;


    @Requires(name = "Blackboard", minorVersion = 1)
    private final Blackboard blackboard = null;

    @Injected
    private EntityRegistry registry;

    private ManeuverStateMap maneuverStateMap = null;

    public ManeuverCentralAgent() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "Central agent for tracking and communicating positions");
    }

    @Override
    public ProcessingResult tick(long currentTime_ms) {
        List<Message> output = new ArrayList<>();
        if (maneuverStateMap != null) {
            Object EntityHandle;
            for (String path : maneuverStateMap.getStateMap().keySet()) {
                Optional<EntityHandle> oeh = registry.getHandle(path);
                oeh.ifPresent(entityHandle -> output.add(prepareMessage(entityHandle)));
            }
        }
        maneuverStateMap = null;
        return ProcessingResult.of(output);
    }

    private Message prepareMessage(final EntityHandle destination) {
        var builder = EntityAction.builder();
        builder.source(Address.makeAddress(ownerHandle, getName()));
        builder.destination(Address.makeExternalAddress(destination));
        builder.content(maneuverStateMap);
        return builder.build();
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(ManeuverState.class);
    }

    @Override
    public void init() {
        super.init();
        if (registry == null) {
            throw new IllegalStateException("EntityRegistry has not been injected");
        }
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms,
                                    Address from, long id, Address respondTo) {
        if (arg instanceof ManeuverState) {
            ManeuverState maneuverState = (ManeuverState) arg;
            if (maneuverStateMap == null) {
                maneuverStateMap = ManeuverStateMap.builder().build();
            }
            maneuverStateMap.addManeuverState(maneuverState);
        } else {
            throw new SSTAFException(arg.getClass() + " is not supported by this Handler");
        }
        return ProcessingResult.empty();
    }

}

