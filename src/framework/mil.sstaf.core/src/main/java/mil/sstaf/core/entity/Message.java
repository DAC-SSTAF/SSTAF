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

package mil.sstaf.core.entity;

import mil.sstaf.core.features.HandlerContent;

/**
 * Defines Objects that can be sent between Entities
 */
public interface Message {

    /**
     * Provides the sequence number associated with the {@code Message}.
     *
     * @return the sequence number for the message
     */
    long getSequenceNumber();

    /**
     * Provides the contents of the message.
     *
     * @return the contents
     */
    HandlerContent getContent();

    /**
     * Provides the {@code Address} of the {@code Entity} and {@code Handler}
     * to which the {@code Message} must go.
     *
     * @return the destination {@code Address}
     */
    Address getDestination();

    /**
     * Provides the {@code Address} of the {@code Entity} and {@code Handler}
     * from which the {@code Message} originated.
     *
     * @return the source {@code Address}
     */
    Address getSource();

    /**
     * Provides the {@code Address} of the {@code Entity} and {@code Handler}
     * to which the destination {@code Entity} and {@code Handler} should send
     * the result of processes the content.
     *
     * @return the respondTo {@code Address}
     */
    Address getRespondTo();

}

