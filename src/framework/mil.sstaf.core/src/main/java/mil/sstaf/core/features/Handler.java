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

import mil.sstaf.core.entity.Address;

import java.util.List;

/**
 * Specifies the interface for all objects that handle message content.
 */
public interface Handler
        extends Feature {

    /**
     * Provides the full Address for the Handler
     *
     * @return the Handler's Address
     */
    Address getAddress();

    /**
     * Provides a descriptive String that fully identifies the Handler
     *
     * @return a descriptive String
     */
    String getInfoString();

    /**
     * Provides a {@code List} of all of the message content {@code Class}es to which this
     * {@code Handle} responds.
     * <p>
     * Handlers can respond to multiple message classes. Only one Handler may be associated with
     * a message class.
     *
     * @return a {@code List} of {@code Class} objects.
     */
    List<Class<? extends HandlerContent>> contentHandled();

    /**
     * Initializes a {@code Handler}.
     * <p>
     * Initialization is invoked after configuration and dependency injection.
     * Init should check that all required dependencies are in place.
     */
    void init();

    /**
     * Performs the processing associated with the given argument at the specified time.
     * <p>
     * Both the scheduled time and current simulation time are reported. For an {@code EntityEvent}
     * the scheduled time value will be less than or equal the current simulation time. For an
     * {@code EntityAction} scheduled time will be the same as the current time. Providing both times
     * enables the Handler to adjust for any time difference between when an event was intended to occur
     * and when it was processed.
     *
     * @param arg              the message content
     * @param scheduledTime_ms the time for which an event was scheduled.
     * @param currentTime_ms   the current simulation time
     * @param from             the sources of the {@code Message}
     * @param id               the message sequence numner
     * @param respondTo        to where to send the results.
     * @return a {@code ProcessingResult} that contains the results of the processing
     */
    ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms,
                             Address from, long id, Address respondTo);


    /**
     * Defines a class that processes {@code Message} content.
     */
    interface ContentProcessor {
        ProcessingResult processMessageContent(HandlerContent content, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo);
    }

}

