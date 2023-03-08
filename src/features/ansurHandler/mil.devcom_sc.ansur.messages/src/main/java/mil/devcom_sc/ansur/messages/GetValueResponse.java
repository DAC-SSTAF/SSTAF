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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.features.DoubleContent;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.IntContent;
import mil.sstaf.core.features.StringContent;
import mil.sstaf.core.util.SSTAFException;

import java.util.Objects;

/**
 * The response from a {@code GetValueMessage}
 */
@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@EqualsAndHashCode(callSuper = true)
public class GetValueResponse extends HandlerContent {
    @Getter
    private final ValueKey key;
    @Getter
    private final HandlerContent value;

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
        StringContent content = StringContent.of(value);
        return builder().key(valueKey).value(content).build();
    }

    public static GetValueResponse of(final ValueKey valueKey, final double value) {
        Objects.requireNonNull(valueKey);
        if (!valueKey.getType().equals(Double.class)) {
            throw new SSTAFException("ValueKey type is not a double");
        }
        DoubleContent content = DoubleContent.of(value);
        return builder().key(valueKey).value(content).build();
    }

    public static GetValueResponse of(final ValueKey valueKey, final int value) {
        Objects.requireNonNull(valueKey);
        if (!valueKey.getType().equals(Integer.class)) {
            throw new SSTAFException("ValueKey type is not a double");
        }
        IntContent content = IntContent.of(value);
        return builder().key(valueKey).value(content).build();
    }

    public static boolean isString(GetValueResponse gvr) {
        return gvr.value instanceof StringContent;
    }

    public static boolean isDouble(GetValueResponse gvr) {
        return gvr.value instanceof DoubleContent;
    }

    public static boolean isInteger(GetValueResponse gvr) {
        return gvr.value instanceof IntContent;
    }

    public static String getStringValue(GetValueResponse gvr) {
        if (GetValueResponse.isString(gvr)) {
            StringContent sc = (StringContent) gvr.value;
            return sc.getValue();
        } else {
            throw new SSTAFException("GetValueResponse does not contain a String");
        }
    }
    public static int getIntegerValue(GetValueResponse gvr) {
        if (GetValueResponse.isInteger(gvr)) {
            IntContent sc = (IntContent) gvr.value;
            return sc.getValue();
        } else {
            throw new SSTAFException("GetValueResponse does not contain an integer");
        }
    }

    public static double getDoubleValue(GetValueResponse gvr) {
        if (GetValueResponse.isDouble(gvr)) {
            DoubleContent sc = (DoubleContent) gvr.value;
            return sc.getValue();
        } else {
            throw new SSTAFException("GetValueResponse does not contain a double");
        }
    }
}

