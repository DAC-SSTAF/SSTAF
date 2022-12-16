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

package mil.sstaf.blackboard.api;

import mil.sstaf.core.features.Handler;

import java.util.Optional;

/**
 * Simple caching mechanism that enables Features to share state information.
 * <p>
 * Values are only valid for a specific time window.
 */
public interface Blackboard extends Handler {

    long BIGBANG = Long.MIN_VALUE;
    long FOREVER = Long.MAX_VALUE;


    /**
     * Retrieves the entry associated with the specified key at the specified time.
     * <p>
     * Type matching is done using isAssignableFrom(), so subclasses will match a query
     * that uses a superclass.
     *
     * @param <T>            Generic type parameter.
     * @param key            the key for which the value is desired.
     * @param currentTime_ms the current simulation time.
     * @param type           The expected class for the value.
     * @return An genericized Optional that holds the value if it exists and is valid, Optional.empty otherwise.
     */
    <T> Optional<T> getEntry(final String key, final long currentTime_ms, Class<T> type);

    /**
     * Returns an Optional containing the value associated with the specified key at the
     * specified time.
     * <p>
     * The value is returned as an Object rather than any specific type.
     *
     * @param key            the key for which the value is desired.
     * @param currentTime_ms the current simulation time.
     * @return a Optional containing the value if the key exists and has a value associated with it and
     * the specified time is within the valid range for the value.
     */
    Optional<Object> getEntry(final String key, final long currentTime_ms);

    /**
     * Adds a non-expiring value to the Blackboard.
     *
     * @param key          the key associated with the value
     * @param value        the value to store
     * @param timestamp_ms the time that the value becomes valid
     */
    void addEntry(final String key, final Object value, final long timestamp_ms);

    /**
     * Adds a value with an expiration time to the Blackboard
     *
     * @param key           the key associated with the value
     * @param value         the value to store
     * @param timestamp_ms  the time at which the value becomes valid
     * @param expiration_ms the time at which the value expires
     */
    void addEntry(final String key, final Object value, final long timestamp_ms, final long expiration_ms);

    /**
     * Forcibly removes any data associated with the given key.
     *
     * @param key the key for which the data should be removed
     */
    void remove(final String key);

}

