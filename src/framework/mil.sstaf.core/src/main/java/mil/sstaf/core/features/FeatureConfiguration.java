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

package mil.sstaf.core.features;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.entity.Rank;

import java.util.Random;

/**
 * Base class for all configurations that can be applied to a {@code Feature.}
 *
 * The original approach was to pass in a {@code JSONObject} and a seed.
 * Converting to Lombok and Jackson made it easier to move deserialization into
 * the framework and provide the {@code Feature} with a real object.
 *
 * {@code Features}s are still responsible for validating their configuration
 * values.
 */
@Jacksonized
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class FeatureConfiguration {
    @Getter
    @Setter
    private long seed;

    public FeatureConfiguration() {
        this.seed = new Random(System.currentTimeMillis() ^ System.nanoTime()).nextLong();
    }
}

