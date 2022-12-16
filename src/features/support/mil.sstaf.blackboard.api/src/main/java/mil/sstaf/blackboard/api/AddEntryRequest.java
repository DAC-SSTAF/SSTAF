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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.HandlerContent;

/**
 * Message class for adding an item to the {@code Blackboard}
 */
@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class AddEntryRequest extends HandlerContent {
    public final String key;
    public final Object value;
    public final long timestamp_ms;
    public final long expiration_ms;

    /**
     * Constructor
     *
     * @param key           the key for the entry
     * @param value         the entry
     * @param timestamp_ms  the time at which the entry becomes valid. Queries before this time will fail.
     * @param expiration_ms the time at which the entry expires.
     */
    public AddEntryRequest(final String key, final Object value, final long timestamp_ms, final long expiration_ms) {
        super();
        this.key = key;
        this.value = value;
        this.timestamp_ms = timestamp_ms;
        this.expiration_ms = expiration_ms;
    }

    /**
     * Constructs a request from an {@code EntityHandle}
     *
     * @param handle the EntityHandle
     * @return a new AddEntryRequest
     */
    public static AddEntryRequest from(EntityHandle handle) {
        return new AddEntryRequest(handle.getPath(), handle, Blackboard.BIGBANG, Blackboard.FOREVER);
    }
}

