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

package mil.sstaftest.maneuver.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.state.State;
import mil.sstaf.core.state.StateProperty;

import java.util.Objects;

/**
 * Immutable description of the current position, heading and velocity of the Entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
public class ManeuverState extends HandlerContent implements State {

    public final String path;

    public final long timestamp_ms;
    public final Position position;
    public final Heading heading;
    public final Speed speed;

    @StateProperty(headerLabel = "position")
    public Position getPosition() {
        return position;
    }

    @StateProperty(headerLabel = "heading")
    public Heading getHeading() {
        return heading;
    }

    @StateProperty(headerLabel =  "speed")
    public Speed getSpeed() {
        return speed;
    }

    /**
     * Creates a ManeuverState
     *
     * @param owner        the EntityHandle for the Entity that produced this record.
     * @param timestamp_ms the simulation time at which the record was created
     * @param position     the Position of the Entity at the time specified by the timestamp
     * @param heading      the Heading of the Entity
     * @param speed        the Speed of the Entity
     * @return a new ManeuverState
     */
    public static ManeuverState of(EntityHandle owner, final long timestamp_ms, final Position position, final Heading heading, final Speed speed) {
        Objects.requireNonNull(owner, "Owner must not be null");
        Objects.requireNonNull(position, "Position must not be null");
        Objects.requireNonNull(heading, "Heading must not be null");
        Objects.requireNonNull(speed, "Speed must not be null");

        ManeuverState.ManeuverStateBuilder<?,?> builder = builder();
        builder.path(owner.getPath());
        builder.timestamp_ms(timestamp_ms);
        builder.position(position);
        builder.heading(heading);
        builder.speed(speed);
        return builder.build();
    }
}

