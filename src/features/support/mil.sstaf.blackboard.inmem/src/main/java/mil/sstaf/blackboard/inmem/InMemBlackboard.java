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

package mil.sstaf.blackboard.inmem;


import mil.sstaf.blackboard.api.*;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.BaseHandler;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;

/**
 * Simple in-memory Map-based implementation of the Blackboard
 */
public class InMemBlackboard extends BaseHandler implements Blackboard {
    public static final String FEATURE_NAME = "Blackboard";
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;
    public static final int PATCH_VERSION = 0;


    private static final Logger logger = LoggerFactory.getLogger(InMemBlackboard.class);
    private final Map<String, Entry> entryMap = new HashMap<>();

    public InMemBlackboard() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION, false,
                "Simple in-memory implementation of a Blackboard");
    }

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
    public <T> Optional<T> getEntry(final String key, final long currentTime_ms, Class<T> type) {
        Objects.requireNonNull(key, "Blackboard query may not use a null key");
        Objects.requireNonNull(type, "Blackboard query may not specify a null type");

        if (logger.isTraceEnabled()) {
            logger.trace("Getting {}/{} valid at {}", key, type.getName(), currentTime_ms);
        }
        Optional<Object> optValue = getEntry(key, currentTime_ms);
        if (optValue.isPresent()) {
            Object val = optValue.get();
            if (type.isAssignableFrom(val.getClass())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Key '{}', returning '{}'", key, val);
                }
                return Optional.of(type.cast(val));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Key '{}' not found, returning Optional.empty", key);
        }
        return Optional.empty();
    }

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
    public Optional<Object> getEntry(final String key, final long currentTime_ms) {
        Object e1 = internalGetEntry(key, currentTime_ms);
        return Optional.ofNullable(e1);
    }

    /**
     * Internal method for querying the Blackboard.
     *
     * @param key            the key for which the value is desired.
     * @param currentTime_ms the current simulation time.
     * @return the value if found and valid, null otherwise.
     */
    private Object internalGetEntry(String key, long currentTime_ms) {
        Objects.requireNonNull(key, "Blackboard query may not use a null key");
        if (logger.isTraceEnabled()) {
            logger.trace("Getting {} valid at {}", key, currentTime_ms);
        }
        Entry e = entryMap.get(key);
        if (e != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Got entry");
            }
            if (currentTime_ms >= e.timestamp_ms) {
                if (currentTime_ms <= e.expiration_ms) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Entry is valid, returning {}", e.value);
                    }
                    return e.value;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Entry is expired, removing it");
                    }
                    entryMap.remove(key);
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("currentTime {} < entry timestamp {}", currentTime_ms, e.timestamp_ms);
                }
            }

        }
        return null;
    }

    /**
     * Adds a non-expiring value to the Blackboard.
     *
     * @param key          the key associated with the value
     * @param value        the value to store
     * @param timestamp_ms the time that the value becomes valid
     */
    public void addEntry(final String key, final Object value, final long timestamp_ms) {
        addEntry(key, value, timestamp_ms, FOREVER);
    }

    /**
     * Adds a value with an expiration time to the Blackboard
     *
     * @param key           the key associated with the value
     * @param value         the value to store
     * @param timestamp_ms  the time at which the value becomes valid
     * @param expiration_ms the time at which the value expires
     */
    public void addEntry(final String key, final Object value, final long timestamp_ms, final long expiration_ms) {
        Objects.requireNonNull(key, "Blackboard entry may not have a null key");
        Objects.requireNonNull(value, "Blackboard entry may not have a null value");
        if (logger.isDebugEnabled()) {
            logger.debug("Adding '{}':'{}'; valid {} to  {}",
                    key, value, timestamp_ms, expiration_ms);
        }
        entryMap.put(key, new Entry(value, timestamp_ms, expiration_ms));
    }

    /**
     * Forcibly removes any data associated with the given key.
     *
     * @param key the key for which the data should be removed
     */
    public void remove(final String key) {
        entryMap.remove(key);
    }

    /**
     * Returns the map that holds the entries.
     * <p>
     * Package-visible for testing.
     *
     * @return an {@code unmodifiableMap} of the entry map;
     */
    Map<String, Entry> getEntryMap() {
        return Collections.unmodifiableMap(entryMap);
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(AddEntryRequest.class, GetEntryRequest.class, RemoveEntryRequest.class);
    }

    @Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        Message output = null;
        if (arg instanceof GetEntryRequest) {
            GetEntryRequest message = (GetEntryRequest) arg;
            Object value = internalGetEntry(message.key, message.time_ms);
            GetEntryResponse response = new GetEntryResponse(value, message.key, message.time_ms, message.type);
            output = buildNormalResponse(response, id, respondTo);
        } else if (arg instanceof RemoveEntryRequest) {
            RemoveEntryRequest rer = (RemoveEntryRequest) arg;
            remove(rer.key);
            RemoveEntryResponse response = new RemoveEntryResponse(entryMap.size());
            output = buildNormalResponse(response, id, respondTo);
        } else if (arg instanceof AddEntryRequest) {
            AddEntryRequest aer = (AddEntryRequest) arg;
            addEntry(aer.key, aer.value, aer.timestamp_ms, aer.expiration_ms);
            AddEntryResponse response = new AddEntryResponse(entryMap.size());
            output = buildNormalResponse(response, id, respondTo);
        }
        if (output == null) {
            return buildUnsupportedMessageResponse(arg, id, respondTo, new UnsupportedOperationException());
        } else {
            return ProcessingResult.of(output);
        }
    }

    static class Entry {

        final long timestamp_ms;
        final long expiration_ms;
        final Object value;

        Entry(Object value, long timestamp_ms, long expiration_ms) {
            this.timestamp_ms = timestamp_ms;
            this.expiration_ms = expiration_ms;
            this.value = value;
        }
    }
}

