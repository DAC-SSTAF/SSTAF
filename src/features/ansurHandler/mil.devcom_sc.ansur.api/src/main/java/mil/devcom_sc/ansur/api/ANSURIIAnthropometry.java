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
package mil.devcom_sc.ansur.api;

import mil.devcom_sc.ansur.messages.Handedness;
import mil.devcom_sc.ansur.messages.Sex;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.features.Handler;

import java.util.Optional;

public interface ANSURIIAnthropometry extends Handler {

    /**
     * Provides the {@code Sex} of the {@code Entity} to which this
     * anthropometry data is assigned.
     *
     * @return the Sex
     */
    Sex getSex();

    /**
     * Provides the height of the {@code Entity} in centimeters to which this
     * anthropometry data is assigned.
     *
     * @return the height in cm.
     */
    double getHeight_cm();

    /**
     * Provides the span of the {@code Entity} in centimeters.
     *
     * @return the height in cm.
     */
    double getSpan_cm();

    /**
     * Provides the weight of the {@code Entity} in kilograms.
     *
     * @return the weight in kg
     */
    double getWeight_kg();

    /**
     * Retrieves a value from the data for this instance.
     *
     * @param key the key for the value to retrieve
     * @return the value if it exists, null if not
     */
    Object getValue(ValueKey key);

    /**
     * Retrieves a value for this instance of anthropometry as a String.
     *
     * @param key the key for the value to retrieve
     * @return an {@code Optional} containing the value if it exists and
     * is a {@code String}. If the value exists but is not a {@code String} the
     * {@code Optional} will be empty.
     */
    Optional<String> getStringValue(ValueKey key);

    /**
     * Retrieves a value for this instance of anthropometry as a Double.
     *
     * @param key the key for the value to retrieve
     * @return an {@code Optional} containing the value if it exists and
     * is a {@code Double}. If the value exists but is not a {@code Double} the
     * {@code Optional} will be empty.
     */
    Optional<Double> getDoubleValue(ValueKey key);

    /**
     * Retrieves a value for this instance of anthropometry as a Integer.
     *
     * @param key the key for the value to retrieve
     * @return an {@code Optional} containing the value if it exists and
     * is a {@code Integer}. If the value exists but is not a {@code Integer} the
     * {@code Optional} will be empty.
     */
    Optional<Integer> getIntegerValue(ValueKey key);

    /**
     * Retrieves the Handedness (LEFT or RIGHT) of the subject
     *
     * @return the Handedness
     */
    Handedness getHandedness();
}

