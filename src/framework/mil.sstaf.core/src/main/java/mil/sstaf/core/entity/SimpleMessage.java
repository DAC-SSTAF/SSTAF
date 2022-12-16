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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import mil.sstaf.core.features.HandlerContent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base implementation of {@code Message}
 */
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode
public abstract class SimpleMessage implements Message {
    protected final static AtomicLong counter = new AtomicLong();

    @Getter
    protected final long sequenceNumber;
    @Getter
    protected final Address destination;
    @Getter
    protected final Address source;
    @Getter
    protected final Address respondTo;
    @Getter
    protected final HandlerContent content;

    protected SimpleMessage(SimpleMessageBuilder<?,?> builder) {
        sequenceNumber = counter.getAndIncrement();
        this.destination = builder.destination;
        this.source = builder.source;
        this.respondTo = builder.respondTo;
        this.content = builder.content;
    }


}

