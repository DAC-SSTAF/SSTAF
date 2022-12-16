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

package mil.sstaftest.maneuver.entityagent;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.EntityEvent;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.BaseAgent;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.features.Requires;
import mil.sstaf.core.util.SSTAFException;
import mil.sstaftest.maneuver.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Agent that holds and updates the position, heading and speed of an Entity.
 */
public class ManeuverEntityAgent extends BaseAgent {
    public static final String FEATURE_NAME = "Maneuver Entity Agent";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    private EntityHandle centralAgentHandle = null;

    @Requires(name = "Blackboard", minorVersion = 1)
    private Blackboard blackboard;

    private static final Logger logger =
            LoggerFactory.getLogger(ManeuverEntityAgent.class);

    private long lastTimeStamp_ms = 0;
    private Heading heading = Heading.of(0.0);
    private Position position = Position.of(0.0, 0.0);
    private Speed speed = Speed.of(0.0);

    public ManeuverEntityAgent() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                false, "Planar maneuver agent");
    }


    private Position updatePosition(final Position position, final Heading heading,
                                    final Speed speed, double deltaT_ms) {
        double distance = speed.speed_ms * (deltaT_ms / 1000.0);
        double newXPos = position.x + distance * Math.cos(heading.heading_rads);
        double newYPos = position.y + distance * Math.sin(heading.heading_rads);
        return Position.of(newXPos, newYPos);
    }

    private ManeuverState updateManeuverState(final long currentTime_ms) {
        ManeuverState state = ManeuverState.of(ownerHandle, currentTime_ms, position, heading, speed);
        blackboard.addEntry("ManeuverState", state, currentTime_ms);
        return state;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public ProcessingResult tick(long currentTime_ms) {
        logger.trace("{} ticking at {}", getInfoString(), currentTime_ms);
        double deltaT = currentTime_ms - lastTimeStamp_ms;
        lastTimeStamp_ms = currentTime_ms;

        if (position == null || heading == null) {
            throw new IllegalStateException("Initial Position and/or Heading are not set!");
        }
        position = updatePosition(position, heading, speed, deltaT);
        ManeuverState state = updateManeuverState(currentTime_ms);

        if (centralAgentHandle == null) {
            Optional<EntityHandle> optHandle = blackboard.getEntry("SYSTEM:EntityController", currentTime_ms, EntityHandle.class);
            optHandle.ifPresentOrElse(
                    obj -> centralAgentHandle = obj,
                    () -> logger.warn("EntityController not set in ManeuverEntityAgent"));
        }

        if (centralAgentHandle == null) {
            logger.debug("{} centralAgent handle is null, returning empty ProcessingResult",
                    getInfoString());
            return ProcessingResult.empty();
        } else {
            var builder = EntityEvent.builder();
            builder.eventTime_ms(currentTime_ms);
            builder.source(Address.makeAddress(ownerHandle, getName()));
            builder.destination(Address.makeExternalAddress(centralAgentHandle));
            builder.content(state);
            EntityEvent event = builder.build();
            logger.trace("{} returning state {}", getInfoString(), state);
            return ProcessingResult.of(event);
        }
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(Heading.class, Position.class, Speed.class, ManeuverStateQuery.class,
                ManeuverState.class, ManeuverStateMap.class);
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {

        logger.debug("{} processing {} value = '{}'",
                getInfoString(), arg.getClass(), arg);

        boolean update = true;
        if (arg instanceof ManeuverState) {
            ManeuverState maneuverState = (ManeuverState) arg;
            logger.trace("{} updating with {}", getInfoString(), maneuverState);
            this.position = maneuverState.position;
            this.heading = maneuverState.heading;
            this.speed = maneuverState.speed;
            logger.trace("{} state = {} {} {}", getInfoString(),
                    this.position, this.heading, this.speed);
        } else if (arg instanceof Position) {
            this.position = (Position) arg;
            logger.trace("{} state = {} {} {}", getInfoString(),
                    this.position, this.heading, this.speed);
        } else if (arg instanceof Heading) {
            this.heading = (Heading) arg;
            logger.trace("{}, state = {} {} {}", getInfoString(),
                    this.position, this.heading, this.speed);
        } else if (arg instanceof Speed) {
            this.speed = (Speed) arg;
            logger.trace("{} state = {} {} {}", getInfoString(),
                    this.position, this.heading, this.speed);
        } else if (arg instanceof ManeuverStateQuery) {
            double deltaT = currentTime_ms - lastTimeStamp_ms;
            updatePosition(position, heading, speed, deltaT);
        } else if (arg instanceof ManeuverStateMap) {
            update = false;
            blackboard.addEntry("ManeuverStateMap", arg, currentTime_ms);
        } else {
            throw new SSTAFException("Can't process " + arg.getClass().getSimpleName());
        }

        if (update) {
            ManeuverState ms = updateManeuverState(currentTime_ms);
            logger.trace("{} returning current state {}", getInfoString(), ms);
            Message response = this.buildNormalResponse(ms, id, respondTo);
            return ProcessingResult.of(response);
        } else {
            return ProcessingResult.empty();
        }
    }

}

