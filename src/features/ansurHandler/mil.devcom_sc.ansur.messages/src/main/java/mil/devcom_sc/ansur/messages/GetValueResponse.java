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

package mil.devcom_sc.ansur.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.util.SSTAFException;

import java.util.Objects;
import java.util.Optional;

/**
 * The response from a {@code GetValueMessage}
 */
@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class GetValueResponse extends HandlerContent {
    private final ValueKey key;
    private final String stringValue;
    private final double doubleValue;
    private final int intValue;

    /**
     * Constructor
     */
    private GetValueResponse(GetValueResponseBuilder<?,?> builder) {
        super(builder);
        Objects.requireNonNull(builder.key);
        key = builder.key;
        if (builder.key.getType().equals(String.class)) {
            Objects.requireNonNull(builder.stringValue);
            stringValue = builder.stringValue;
            doubleValue = Double.MIN_VALUE;
            intValue = Integer.MIN_VALUE;
        } else if (builder.key.getType().equals(Double.class)) {
            doubleValue = builder.doubleValue;
            intValue = Integer.MIN_VALUE;
            stringValue = null;
        } else if (builder.key.getType().equals(Integer.class)) {
            intValue = builder.intValue;
            doubleValue = Double.MIN_VALUE;
            stringValue = null;
        } else {
            throw new SSTAFException("Value key has unsupported type");
        }
    }

    /**
     * Factory method for making a GetValueResponse with a String.
     *
     * @param value the value to include
     * @return the response object.
     */
    public static GetValueResponse of(final ValueKey valueKey, final String value) {
        Objects.requireNonNull(valueKey);
        if (!valueKey.getType().equals(String.class)) {
            throw new SSTAFException("ValueKey type is not a String");
        }
        return builder().key(valueKey).stringValue(value).build();
    }

    public static GetValueResponse of(final ValueKey valueKey, final double value) {
        Objects.requireNonNull(valueKey);
        if (!valueKey.getType().equals(Double.class)) {
            throw new SSTAFException("ValueKey type is not a double");
        }
        return builder().key(valueKey).doubleValue(value).build();
    }

    public static GetValueResponse of(final ValueKey valueKey, final int value) {
        Objects.requireNonNull(valueKey);
        if (! valueKey.getType().equals(Integer.class)) {
            throw new SSTAFException("ValueKey type is not a double");
        }
        return builder().key(valueKey).intValue(value).build();
    }

    /**
     * Returns the value wrapped in an {@code Optional}.
     *
     * @return an {@code Optional} containing the value or empty.
     */
    public Optional<Object> getValue() {
        if (key.getType().equals(String.class)) {
           return Optional.of(stringValue);
        } else if (key.getType().equals(Double.class)) {
            return Optional.of(doubleValue);
        } else if (key.getType().equals(Integer.class)) {
           return Optional.of(intValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Provides the value wrapped in an {@code Optional} if it is a {@code String}.
     *
     * @return an {@code Optional} containing the value or empty.
     */
    public Optional<String> getStringValue() {
        return Optional.ofNullable(stringValue);
    }

    /**
     * Provides the value wrapped in an {@code Optional} if it is a {@code Double}.
     *
     * @return an {@code Optional} containing the value or empty.
     */
    public Optional<Double> getDoubleValue() {
        if (key.getType().equals(Double.class)) {
            return Optional.of(doubleValue);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Provides the value wrapped in an {@code Optional} if it is a {@code Integer}.
     *
     * @return an {@code Optional} containing the value or empty.
     */
    public Optional<Integer> getIntegerValue() {
        if (key.getType().equals(Integer.class)) {
            return Optional.of(intValue);
        } else {
            return Optional.empty();
        }
    }
}

