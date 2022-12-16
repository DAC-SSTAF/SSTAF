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

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface for all object's that communicate via asynchronous {@code Message} objects.
 * <p>
 * Each instance of {@code MessageDriven} objects must have a globally unique identification number. This
 * ID number is used to facilitate a repeatable global total ordering of messages and events within the
 * system. The {@code MessageDriven.IDProvider.getID()} method can be used to provide the indentification number.
 */
public interface MessageDriven {

    long CLIENT_APP_ID = -1776;

    /**
     * Adds a {@link Message} to this object's {@code inboundQueue}.
     *
     * @param message the message to add
     */
    void receive(Message message);

    /**
     * Takes all of the messages currently enqueued in this object's {@code inboundQueue}.
     *
     * @return a {@link java.util.List} of all of the inbound messages;
     */
    List<Message> takeInbound();

    /**
     * Takes all of the messages currently enqueued in this object's {@code outboundQueue}.
     *
     * @return a {@link java.util.List} of all of the outbound messages;
     */
    List<Message> takeOutbound();

    /**
     * Provides the unique ID number for this {@code MessageDriven} object.
     *
     * @return the ID number.
     */
    long getId();

    /**
     * Provides a message sequence number that is unique and ordered within the context of
     * this {@code MessageDriven} object.
     * <p>
     * The sequence number supports the global total order mechanism.
     *
     * @return the sequence number
     */
    long generateSequenceNumber();

    class BlockCounter {
        public static final long SYSTEM_BLOCK_BEGIN = 0L;
        public static final long USER_BLOCK_BEGIN = 10000L;
        public static BlockCounter systemCounter
                = new BlockCounter("System", SYSTEM_BLOCK_BEGIN, USER_BLOCK_BEGIN);
        public static BlockCounter userCounter
                = new BlockCounter("User", USER_BLOCK_BEGIN, Long.MAX_VALUE);
        private final AtomicLong counter;
        private final long upperValue;
        private final String name;

        private BlockCounter(final String name, final long lowerValue, final long upperValue) {
            this.name = name;
            this.counter = new AtomicLong(lowerValue);
            this.upperValue = upperValue;
        }

        public long getID() {
            if (counter.get() == upperValue - 1) {
                throw new IllegalStateException(name + " entity id counter has exceeded maximum supported entity count");
            }
            return counter.getAndIncrement();
        }
    }

}

