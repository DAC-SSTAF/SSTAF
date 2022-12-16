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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.features.HandlerContent;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class Heading extends HandlerContent {
    public final double heading_degs;
    public final double heading_rads;

    private Heading(final double headingDegrees) {
        this.heading_degs = headingDegrees;

        double constrained = (heading_degs <= 180.0) ? heading_degs : heading_degs - 360.0;
        double flipped = 90.0 - constrained;
        heading_rads = flipped * Math.PI / 180.0;
    }

    public static Heading of(final double headingDeg) {
        if (headingDeg < 0.0 || headingDeg > 360.0) {
            throw new IllegalArgumentException("Heading must be in the range [0.0,360.0]");
        }
        return new Heading(headingDeg);
    }

    @Override
    public String toString() {
        return "Heading{" +
                "heading_degs=" + heading_degs +
                ", heading_rads=" + heading_rads +
                '}';
    }
}

